package dev.powerstats.api
package route

import route.request.*
import service.ApiKeyServiceComponent

import dev.powerstats.common.db.model.ApiKey
import cats.effect.*
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import org.http4s.dsl.io.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

trait ApiKeyRoutesComponent {
  this: RoutesComponent &
    ApiKeyServiceComponent =>
  val apiKeyRoutes: ApiKeyRoutes

  trait ApiKeyRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findEndpoint = routes.secureEndpoint.get
        .in("api" / "v1" / "api-key")
        .out(jsonBody[ApiSuccessResponseWithData[List[ApiKey]]])

      val findServerEndpoint = findEndpoint.serverLogic(accountId => _ =>
        routes.responseWithData(apiKeyService.findApiKeys(accountId, xa))
      )

      val createEndpoint = routes.secureEndpoint.post
        .in("api" / "v1" / "api-key")
        .in(jsonBody[ApiKeyCreateRequest])
        .out(jsonBody[ApiSuccessResponseWithData[ApiKeyCreateResponse]])

      val createServerEndpoint = createEndpoint.serverLogic(accountId => createRequest =>
        routes.responseWithData(apiKeyService.createApiKey(accountId, createRequest.name, xa)
          .map((key, apiKey) => ApiKeyCreateResponse(key, apiKey)))
      )

      val deleteEndpoint = routes.secureEndpoint.delete
        .in("api" / "v1" / "api-key" / path[Long]("id"))
        .out(jsonBody[ApiSuccessResponse])

      val deleteServerEndpoint = deleteEndpoint.serverLogic(accountId => id =>
        routes.response(apiKeyService.deleteApiKey(id, accountId, xa))
      )

      List(findServerEndpoint, createServerEndpoint, deleteServerEndpoint)
    }
  }
}
