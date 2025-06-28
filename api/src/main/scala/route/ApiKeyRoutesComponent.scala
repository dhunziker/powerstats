package dev.powerstats.api
package route

import route.request.*
import route.request.ApiResponse.given
import service.{ApiKeyServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.ApiKey
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
    SecurityServiceComponent &
    ApiKeyServiceComponent =>
  val apiKeyRoutes: ApiKeyRoutes

  trait ApiKeyRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "api-key")
        .description("Endpoint to retrieve a list of API keys for the authenticated account.")
        .out(jsonBody[ApiSuccessResponseWithData[List[ApiKey]]]
          .description("Response containing a list of API keys associated with the account."))

      val findServerEndpoint = findEndpoint.serverLogic(accountId => _ =>
        routes.responseWithData(apiKeyService.findApiKeys(accountId, xa))
      )

      val createEndpoint = routes.secureEndpoint(securityService, xa).post
        .in("v1" / "api-key")
        .description("Endpoint to create a new API key for the authenticated account.")
        .in(jsonBody[ApiKeyCreateRequest]
          .description("Request body containing the name of the new API key."))
        .out(jsonBody[ApiSuccessResponseWithData[ApiKeyCreateResponse]]
          .description("Response containing the created API key and its details."))

      val createServerEndpoint = createEndpoint.serverLogic(accountId => createRequest =>
        routes.responseWithData(apiKeyService.createApiKey(accountId, createRequest.name, xa)
          .map((key, apiKey) => ApiKeyCreateResponse(key, apiKey)))
      )

      val deleteEndpoint = routes.secureEndpoint(securityService, xa).delete
        .in("v1" / "api-key" / path[Long]("id").description("The ID of the API key to be deleted."))
        .description("Endpoint to delete an API key by its ID for the authenticated account.")
        .out(jsonBody[ApiSuccessResponse]
          .description("Response indicating the success or failure of the deletion operation."))

      val deleteServerEndpoint = deleteEndpoint.serverLogic(accountId => id =>
        routes.response(apiKeyService.deleteApiKey(id, accountId, xa))
      )

      List(findServerEndpoint, createServerEndpoint, deleteServerEndpoint)
    }
  }
}
