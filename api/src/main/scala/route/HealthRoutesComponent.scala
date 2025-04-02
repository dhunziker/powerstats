package ai.powerstats.api
package route

import cats.effect.*
import doobie.*
import doobie.implicits.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*

trait HealthRoutesComponent {
  val healthRoutes: HealthRoutes

  trait HealthRoutes extends Routes {
    override def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
      case GET -> Internal / "health-check" =>
        sql"select 1"
          .query[Int]
          .unique
          .transact(xa)
          .attempt
          .flatMap {
            case Left(value) => IO.raiseError(value)
            case Right(value) => Ok("Ok")
          }
    }
  }
}
