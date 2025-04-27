package ai.powerstats.api
package route

import route.request.*
import service.AccountServiceComponent

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.*
import doobie.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io.*
import org.typelevel.log4cats.LoggerFactory

import java.time.Clock

trait AccountRoutesComponent {
  this: ConfigComponent &
    LoggingComponent &
    AccountServiceComponent =>
  val accountRoutes: AccountRoutes

  trait AccountRoutes extends PublicRoutes {
    private val logger = LoggerFactory[IO].getLogger
    private implicit val clock: Clock = Clock.systemDefaultZone()

    override def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
      case req@POST -> Root / "user" / "register" => for {
        registerRequest <- req.as[UserRegisterRequest]
        response <- handleResponse(accountService.register(registerRequest.email, registerRequest.password, xa), logger)
      } yield response

      case req@POST -> Root / "user" / "activate" => for {
        activateRequest <- req.as[UserActivateRequest]
        response <- handleResponse(accountService.activate(activateRequest.activationKey, xa).map { (account, webToken) =>
          UserActivateResponse(account.email, webToken)
        }, logger)
      } yield response

      case req@POST -> Root / "user" / "login" => for {
        loginRequest <- req.as[UserLoginRequest]
        response <- accountService.login(loginRequest.email, loginRequest.password, xa)
          .attempt
          .flatMap {
            case Left(err) => IO.pure(err.getMessage)
              .flatMap(message => logger.error(message))
              .map(_ => Response[IO](status = Unauthorized))
            // TODO: Handle this through exception handleResponse
            case Right(token) => Ok(UserLoginResponse(loginRequest.email, token).asJson)
          }
      } yield response
    }
  }
}
