package ai.powerstats.api
package route

import service.ApiKeyServiceComponent

import ai.powerstats.common.logging.LoggingComponent
import cats.effect.*
import doobie.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.LoggerFactory

trait ApiKeyRoutesComponent {
  this: LoggingComponent & ApiKeyServiceComponent =>
  val apiKeyRoutes: ApiKeyRoutes

  trait ApiKeyRoutes extends SecuredRoutes {
    private val logger = LoggerFactory[IO].getLogger

    override def routes(xa: Transactor[IO]): AuthedRoutes[Long, IO] = AuthedRoutes.of {
      case GET -> Root / "api-key" as userId => for {
        _ <- logger.info(s"Request from user with ID $userId")
        response <- apiKeyService.findApiKey("", xa)
          .attempt
          .flatMap {
            case Left(err) => IO.pure(err.getMessage)
              .flatMap(message => logger.error(message))
              .flatMap(message => InternalServerError(message))
            case Right(apiKey) => Ok(apiKey.asJson)
          }
      } yield response
    }
  }
}
