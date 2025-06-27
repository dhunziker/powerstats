package dev.powerstats.api
package route

import route.request.ApiResponse.given
import route.request.ApiSuccessResponseWithData
import service.{LifterServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.{Event, PersonalBest}
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.time.LocalDate

trait LifterRoutesSecuredComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    LifterServiceComponent =>
  val lifterRoutesSecured: LifterRoutesSecured

  trait LifterRoutesSecured {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findLiftersEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter")
        .in(query[String]("namePattern"))
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[String]]])
        .attribute(Attributes.rateLimit, 60)

      val findLiftersServerEndpoint = findLiftersEndpoint.serverLogic(accountId => (namePattern, limit) =>
        routes.responseWithData(for {
          lifters <- lifterService.findLifters(namePattern, limit, xa)
        } yield lifters)
      )

      val findPersonalBestsEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter" / path[String]("name") / "personal-bests")
        .out(jsonBody[ApiSuccessResponseWithData[List[PersonalBest]]])
        .attribute(Attributes.rateLimit, 10)

      val findPersonalBestsServerEndpoint = findPersonalBestsEndpoint.serverLogic(accountId => name =>
        routes.responseWithData(for {
          personalBests <- lifterService.findPersonalBest(name, xa)
        } yield personalBests)
      )

      val findCompetitionResultsEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter" / path[String]("name") / "competition-results")
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[Event]]])
        .attribute(Attributes.rateLimit, 10)

      val findCompetitionResultsServerEndpoint = findCompetitionResultsEndpoint.serverLogic(accountId => (name, limit) =>
        routes.responseWithData(for {
          events <- lifterService.findCompetitionResults(name, limit, xa)
        } yield events)
      )

      List(findLiftersServerEndpoint, findPersonalBestsServerEndpoint, findCompetitionResultsServerEndpoint)
    }
  }
}
