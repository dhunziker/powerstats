package dev.powerstats.api
package route

import route.request.*
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
        .in(jsonBody[UserRegisterRequest])
        .out(jsonBody[ApiSuccessResponseWithData[Account]])

      val registerServerEndpoint = registerEndpoint.serverLogic(registerRequest =>
        routes.responseWithData(accountService.register(registerRequest.email, registerRequest.password, xa))
      )

      val loginEndpoint = routes.publicEndpoint.post
        .in("user" / "login")
        .in(jsonBody[UserLoginRequest])
        .out(jsonBody[ApiSuccessResponseWithData[UserLoginResponse]])

      val loginServerEndpoint = loginEndpoint.serverLogic(loginRequest =>
        routes.responseWithData(
          accountService.login(loginRequest.email, loginRequest.password, xa).map { token =>
            UserLoginResponse(loginRequest.email, token)
          }
          , isSecurityLogic = true)
      )

      val activateEndpoint = routes.publicEndpoint.post
        .in("user" / "activate")
        .in(jsonBody[UserActivateRequest])
        .out(jsonBody[ApiSuccessResponseWithData[UserActivateResponse]])

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
