package dev.powerstats.api
package route

import error.TooManyRequestsError

import cats.effect.IO
import com.github.benmanes.caffeine.cache.Caffeine
import scalacache.*
import scalacache.caffeine.CaffeineCache
import sttp.monad.MonadError
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.*

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
      endpoints.headOption.flatMap(_.attribute(Attributes.rateLimit)) match {
        case Some(rateLimit) =>
          val ipAddress = request.connectionInfo.remote.map(_.getAddress.getHostAddress).getOrElse("unknown")
          val key = cacheKey(ipAddress)
          for {
            buffer <- cache.cachingF(key)(None)(IO.pure(rateLimit))
            _ <- IO.raiseUnless(buffer > 0)(new TooManyRequestsError(s"Reached $rateLimit requests per minute"))
            _ <- cache.put(key)(buffer - 1)
            response <- next(request, endpoints)
          } yield response
        case None => next(request, endpoints)
      }
    }

    private def cacheKey(ipAddress: String): String = {
      val currentEpochMinute = Instant.now().toEpochMilli / 60000
      s"$ipAddress/$currentEpochMinute"
    }
  }
}
