package ai.powerstats.api
package route

import error.ServiceUnavailableError
import route.request.ApiSuccessResponse

import ai.powerstats.common.db.HealthRepositoryComponent
import cats.effect.*
import doobie.*
import io.circe.generic.auto.*
import org.http4s.dsl.io.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

trait HealthRoutesComponent {
  this: RoutesComponent &
    HealthRepositoryComponent =>
  val healthRoutes: HealthRoutes

  trait HealthRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val healthEndpoint = routes.publicEndpoint.get
        .in("health-check")
        .out(jsonBody[ApiSuccessResponse])

      val findServerEndpoint = healthEndpoint.serverLogic(_ =>
        routes.response(
          healthRepository.checkHealth(xa).flatMap { result =>
            IO.raiseUnless(result)(ServiceUnavailableError("Health check failed"))
          }.adaptError {
            case err => ServiceUnavailableError(err.getMessage)
          }
        )
      )

      List(findServerEndpoint)
    }
  }
}
