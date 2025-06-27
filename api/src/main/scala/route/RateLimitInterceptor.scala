package dev.powerstats.api
package route

import error.TooManyRequestsError
import route.request.ApiErrorResponse.{*, given}
import route.request.ApiResponse.given
import route.request.{ApiError, ApiErrorResponse}

import cats.*
import cats.effect.*
import com.github.benmanes.caffeine.cache.Caffeine
import io.circe.generic.auto.*
import scalacache.*
import scalacache.caffeine.CaffeineCache
import sttp.model.StatusCode
import sttp.model.StatusCode.TooManyRequests
import sttp.monad.MonadError
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.*
import sttp.tapir.server.model.ValuedEndpointOutput

import java.time.Instant
import scala.concurrent.duration.*
import scala.jdk.DurationConverters.*

class RateLimitInterceptor extends RequestInterceptor[IO] {
  private val underlyingCache = Caffeine.newBuilder()
    .maximumSize(300L)
    .expireAfterAccess(1.minute.toJava)
    .build[String, Entry[Int]]
  private val cache: Cache[IO, String, Int] = CaffeineCache(underlyingCache)

  override def apply[R, B](responder: Responder[IO, B], requestHandler: EndpointInterceptor[IO] => RequestHandler[IO, R, B]) = new RequestHandler[IO, R, B] {
    private val next = requestHandler(EndpointInterceptor.noop)

    override def apply(request: ServerRequest, endpoints: List[ServerEndpoint[R, IO]])(implicit monad: MonadError[IO]) = {
      endpoints.headOption.flatMap { endpoint =>
        endpoint.attribute(Attributes.rateLimit).map { rateLimit =>
          (endpoint.hashCode(), rateLimit)
        }
      } match {
        case Some(endpointId, rateLimit) =>
          for {
            key <- IO.pure(cacheKey(request, endpointId))
            buffer <- cache.cachingF(key)(None)(IO.pure(rateLimit))
            response <- if (buffer > 0) {
              next(request, endpoints)
            } else {
              tooManyRequests(request, rateLimit)
            }
            _ <- cache.put(key)(buffer - 1)
          } yield response
        case _ =>
          next(request, endpoints)
      }
    }

    private def cacheKey(request: ServerRequest, endpointId: Int): String = {
      val ipAddress = request.connectionInfo.remote.map(_.getAddress.getHostAddress).getOrElse("unknown")
      val currentEpochMinute = Instant.now().toEpochMilli / 60000
      s"$ipAddress/$endpointId/$currentEpochMinute"
    }

    private def tooManyRequests(request: ServerRequest, rateLimit: Int) = {
      responder.apply(
        request,
        ValuedEndpointOutput(statusCode(TooManyRequests).and(jsonBody[ApiErrorResponse]),
          ApiErrorResponse(TooManyRequests, TooManyRequestsError(s"Reached $rateLimit requests per minute")))
      ).map(RequestResult.Response.apply)
    }
  }
}
