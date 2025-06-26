package dev.powerstats.api
package route

import error.ServiceUnavailableError
import route.request.ApiResponse.given
import route.request.ApiSuccessResponse

import cats.effect.*
import dev.powerstats.common.db.HealthRepositoryComponent
import doobie.*
import io.circe.generic.auto.*
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
        .attribute(Attributes.rateLimit, 60)

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
