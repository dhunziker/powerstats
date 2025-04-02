package ai.powerstats.api
package route

import route.request.{AccountAuthRequest, AccountRegisterRequest}
import service.AccountServiceComponent

import cats.effect.*
import doobie.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*

trait AccountRoutesComponent {
  this: AccountServiceComponent =>
  val accountRoutes: AccountRoutes

  trait AccountRoutes extends Routes {
    override def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
      case req@POST -> Internal / "account" / "register" => for {
        parsedRequest <- req.as[AccountRegisterRequest]
        response <- accountService.register(parsedRequest.email, parsedRequest.password, xa)
          .redeemWith(_ => IO.pure(Response[IO](status = Unauthorized)), account => Ok(account.asJson))
      } yield response

      case req@POST -> Internal / "account" / "auth" => for {
        loginRequest <- req.as[AccountAuthRequest]
        response <- accountService.auth(loginRequest.email, loginRequest.password, xa)
          .redeemWith(_ => IO.pure(Response[IO](status = Unauthorized)), account => Ok(account.asJson))
      } yield response
    }
  }
}
