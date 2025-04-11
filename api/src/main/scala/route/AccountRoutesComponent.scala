package ai.powerstats.api
package route

import route.request.{AccountAuthRequest, AccountRegisterRequest}
import service.AccountServiceComponent

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

trait AccountRoutesComponent {
  this: LoggingComponent & AccountServiceComponent =>
  val accountRoutes: AccountRoutes

  trait AccountRoutes extends Routes {
    private val logger = LoggerFactory[IO].getLogger

    override def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
      case req@POST -> Root / "api" / "v1" / "account" / "register" => for {
        parsedRequest <- req.as[AccountRegisterRequest]
        response <- accountService.register(parsedRequest.email, parsedRequest.password, xa)
          .attempt
          .flatMap {
            case Left(err) => IO.pure(err.getMessage)
              .flatMap(message => logger.error(message))
              .flatMap(message => InternalServerError(message))
            case Right(_) => Ok()
          }
      } yield response

      case req@POST -> Root / "api" / "v1" / "account" / "login" => for {
        loginRequest <- req.as[AccountAuthRequest]
        response <- accountService.login(loginRequest.email, loginRequest.password, xa)
          .attempt
          .flatMap {
            case Left(err) => IO.pure(err.getMessage)
              .flatMap(message => logger.error(message))
              .map(_ => Response[IO](status = Unauthorized))
            case Right(token) => Ok(token.asJson)
          }
      } yield response
    }
  }
}
