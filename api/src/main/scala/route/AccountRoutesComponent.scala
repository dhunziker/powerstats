package dev.powerstats.api
package route

import route.request.*
import route.request.ApiResponse.given
import service.AccountServiceComponent

import cats.effect.*
import dev.powerstats.common.db.model.Account
import doobie.*
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.dsl.io.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

trait AccountRoutesComponent {
  this: RoutesComponent &
    AccountServiceComponent =>
  val accountRoutes: AccountRoutes

  trait AccountRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val registerEndpoint = routes.publicEndpoint.post
        .in("user" / "register")
        .description("Endpoint to register a new user account.")
        .in(jsonBody[UserRegisterRequest].description("Request body containing email and password for registration."))
        .out(jsonBody[ApiSuccessResponseWithData[Account]].description("Response containing the created account details."))
        .attribute(Attributes.rateLimit, 10)

      val registerServerEndpoint = registerEndpoint.serverLogic(registerRequest =>
        routes.responseWithData(accountService.register(registerRequest.email, registerRequest.password, xa))
      )

      val loginEndpoint = routes.publicEndpoint.post
        .in("user" / "login")
        .description("Endpoint to authenticate a user and provide an authentication token.")
        .in(jsonBody[UserLoginRequest].description("Request body containing email and password for login."))
        .out(jsonBody[ApiSuccessResponseWithData[UserLoginResponse]].description("Response containing the user's email and authentication token."))
        .attribute(Attributes.rateLimit, 10)

      val loginServerEndpoint = loginEndpoint.serverLogic(loginRequest =>
        routes.responseWithData(
          accountService.login(loginRequest.email, loginRequest.password, xa).map { token =>
            UserLoginResponse(loginRequest.email, token)
          }
          , isSecurityLogic = true)
      )

      val activateEndpoint = routes.publicEndpoint.post
        .in("user" / "activate")
        .description("Endpoint to activate a user account using an activation key.")
        .in(jsonBody[UserActivateRequest].description("Request body containing the activation key."))
        .out(jsonBody[ApiSuccessResponseWithData[UserActivateResponse]].description("Response containing the user's email and web token."))
        .attribute(Attributes.rateLimit, 10)

      val activateServerEndpoint = activateEndpoint.serverLogic(activateRequest =>
        routes.responseWithData(
          accountService.activate(activateRequest.activationKey, xa).map { (account, webToken) =>
            UserActivateResponse(account.email, webToken)
          }
        )
      )

      List(registerServerEndpoint, loginServerEndpoint, activateServerEndpoint)
    }
  }
}
