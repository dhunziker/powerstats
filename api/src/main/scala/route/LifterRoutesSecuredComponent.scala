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
        .description("Endpoint to retrieve a list of lifters matching a name pattern.")
        .in(query[String]("namePattern").description("Pattern to match lifter names."))
        .in(query[Int]("limit").default(100).description("Limit the number of results returned."))
        .out(jsonBody[ApiSuccessResponseWithData[List[String]]].description("Response containing a list of lifter names matching the pattern."))
        .attribute(Attributes.rateLimit, 60)

      val findLiftersServerEndpoint = findLiftersEndpoint.serverLogic(accountId => (namePattern, limit) =>
        routes.responseWithData(for {
          lifters <- lifterService.findLifters(namePattern, limit, xa)
        } yield lifters)
      )

      val findPersonalBestsEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter" / path[String]("name").description("Name of the lifter whose personal bests are to be retrieved.") / "personal-bests")
        .description("Endpoint to retrieve personal bests for a specific lifter.")
        .out(jsonBody[ApiSuccessResponseWithData[List[PersonalBest]]].description("Response containing a list of personal bests for the lifter."))
        .attribute(Attributes.rateLimit, 10)

      val findPersonalBestsServerEndpoint = findPersonalBestsEndpoint.serverLogic(accountId => name =>
        routes.responseWithData(for {
          personalBests <- lifterService.findPersonalBest(name, xa)
        } yield personalBests)
      )

      val findCompetitionResultsEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter" / path[String]("name").description("Name of the lifter whose competition results are to be retrieved.") / "competition-results")
        .description("Endpoint to retrieve competition results for a specific lifter.")
        .in(query[Int]("limit").default(100).description("Limit the number of results returned."))
        .out(jsonBody[ApiSuccessResponseWithData[List[Event]]].description("Response containing a list of competition results for the lifter."))
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
