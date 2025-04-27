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

  trait ApiKeyRoutes extends RoutesWithAccountId {
    private val logger = LoggerFactory[IO].getLogger

    override def routes(xa: Transactor[IO]): AuthedRoutes[Long, IO] = AuthedRoutes.of {
      case GET -> Root / "api-key" as accountId =>
        handleResponse(apiKeyService.findApiKeys(accountId, xa), logger)

      case authReq@POST -> Root / "api-key" as accountId => for {
        parsedRequest <- authReq.req.as[ApiKeyCreateRequest]
        response <- handleResponse(apiKeyService.createApiKey(accountId, parsedRequest.name, xa)
          .map((key, apiKey) => ApiKeyCreateResponse(key, apiKey)), logger)
      } yield response

      case DELETE -> Root / "api-key" / LongVar(id) as accountId =>
        handleResponse(apiKeyService.deleteApiKey(id, accountId, xa), logger)
    }
  }
}
