package ai.powerstats.api
package route

import cats.effect.*
import doobie.*
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.Logger

trait Routes {
  def handleResponse[A](response: IO[A], logger: Logger[IO])(implicit encoder: Encoder[A]) = {
    response.flatMap {
        case data: Unit => Ok()
        case data => Ok(data.asJson)
      }
      .onError {
        case err => logger.error(err.getMessage)
      }
      .handleErrorWith(err => InternalServerError(err.getMessage))
  }
}

trait RoutesWithUserId extends Routes {
  def routes(xa: Transactor[IO]): AuthedRoutes[Long, IO]
}

trait OpenRoutes extends Routes {
  def routes(xa: Transactor[IO]): HttpRoutes[IO]
}
