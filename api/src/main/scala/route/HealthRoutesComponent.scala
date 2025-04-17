package ai.powerstats.api
package route

import ai.powerstats.common.db.HealthRepositoryComponent
import cats.effect.*
import doobie.*
import org.http4s.*
import org.http4s.dsl.io.*

trait HealthRoutesComponent {
  this: HealthRepositoryComponent =>
  val healthRoutes: HealthRoutes

  trait HealthRoutes extends OpenRoutes {
    override def routes(xa: Transactor[IO]) = HttpRoutes.of[IO] {
      case GET -> Root / "health-check" => for {
        status <- healthRepository.checkHealth(xa)
        response <- {
          if (status) Ok("Ok")
          else ServiceUnavailable("Health check failed")
        }
      } yield response
    }
  }
}
