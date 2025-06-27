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

trait LifterRoutesPublicComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    LifterServiceComponent =>
  val lifterRoutesPublic: LifterRoutesPublic

  trait LifterRoutesPublic {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findLiftersEndpoint = routes.publicEndpoint.get
        .in("lifter")
        .in(query[String]("namePattern"))
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[String]]])
        .attribute(Attributes.rateLimit, 60)

      val findLiftersServerEndpoint = findLiftersEndpoint.serverLogic((namePattern, limit) =>
        routes.responseWithData(for {
          lifters <- lifterService.findLifters(namePattern, limit, xa)
        } yield lifters)
      )

      val findPersonalBestsEndpoint = routes.publicEndpoint.get
        .in("lifter" / path[String]("name") / "personal-bests")
        .out(jsonBody[ApiSuccessResponseWithData[List[PersonalBest]]])
        .attribute(Attributes.rateLimit, 10)

      val findPersonalBestsServerEndpoint = findPersonalBestsEndpoint.serverLogic(name =>
        routes.responseWithData(for {
          personalBests <- lifterService.findPersonalBest(name, xa)
        } yield personalBests)
      )

      val findCompetitionResultsEndpoint = routes.publicEndpoint.get
        .in("lifter" / path[String]("name") / "competition-results")
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[Event]]])
        .attribute(Attributes.rateLimit, 10)

      val findCompetitionResultsServerEndpoint = findCompetitionResultsEndpoint.serverLogic((name, limit) =>
        routes.responseWithData(for {
          events <- lifterService.findCompetitionResults(name, limit, xa)
        } yield events)
      )

      List(findLiftersServerEndpoint, findPersonalBestsServerEndpoint, findCompetitionResultsServerEndpoint)
    }
  }
}
