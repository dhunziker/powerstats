package ai.powerstats.api
package route

import route.request.{ApiKeyCreateRequest, ApiKeyCreateResponse}
import service.ApiKeyServiceComponent

import ai.powerstats.common.logging.LoggingComponent
import cats.effect.*
import doobie.*
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import org.typelevel.log4cats.LoggerFactory

trait ApiKeyRoutesComponent {
  this: LoggingComponent &
    ApiKeyServiceComponent =>
  val apiKeyRoutes: ApiKeyRoutes

  trait ApiKeyRoutes extends RoutesWithUserId {
    private val logger = LoggerFactory[IO].getLogger

    override def routes(xa: Transactor[IO]): AuthedRoutes[Long, IO] = AuthedRoutes.of {
      case GET -> Root / "api-key" as userId =>
        handleResponse(apiKeyService.findApiKeys(userId, xa), logger)

      case authReq@POST -> Root / "api-key" as userId => for {
        parsedRequest <- authReq.req.as[ApiKeyCreateRequest]
        response <- handleResponse(apiKeyService.createApiKey(userId, parsedRequest.name, xa)
          .map((key, apiKey) => ApiKeyCreateResponse(key, apiKey)), logger)
      } yield response

      case DELETE -> Root / "api-key" / LongVar(id) as userId =>
        handleResponse(apiKeyService.deleteApiKey(id, userId, xa), logger)
    }
  }
}
