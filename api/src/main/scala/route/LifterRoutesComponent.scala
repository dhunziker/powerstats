package dev.powerstats.api
package route

import route.request.ApiResponse.given
import route.request.ApiSuccessResponseWithData
import service.{LifterServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.PersonalBest
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.time.LocalDate

trait LifterRoutesComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    LifterServiceComponent =>
  val lifterRoutes: LifterRoutes

  trait LifterRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findLiftersEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter")
        .in(query[String]("namePattern"))
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[String]]])

      val findLiftersServerEndpoint = findLiftersEndpoint.serverLogic(accountId => (namePattern, limit) =>
        routes.responseWithData(for {
          lifters <- lifterService.findLifters(namePattern, limit, xa)
        } yield lifters)
      )

      val findPersonalBestsEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter" / "personal-best" / path[String]("name"))
        .out(jsonBody[ApiSuccessResponseWithData[List[PersonalBest]]])

      val findPersonalBestsServerEndpoint = findPersonalBestsEndpoint.serverLogic(accountId => name =>
        routes.responseWithData(for {
          personalBests <- lifterService.findPersonalBest(name, xa)
        } yield personalBests)
      )

      List(findLiftersServerEndpoint, findPersonalBestsServerEndpoint)
    }
  }
}
