package dev.powerstats.api
package route

import route.request.ApiResponse.given
import route.request.ApiSuccessResponseWithData
import service.{MeetServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.Meet
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.time.LocalDate

trait MeetRoutesComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    MeetServiceComponent =>
  val meetRoutes: MeetRoutes

  trait MeetRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findMeetEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "meet")
        .description("Endpoint to retrieve a list of meets based on various filters.")
        .in(query[Option[String]]("federation").description("Filter by federation name."))
        .in(query[Option[LocalDate]]("date").description("Filter by meet date."))
        .in(query[Option[String]]("meetCountry").description("Filter by meet country."))
        .in(query[Option[String]]("meetName").description("Filter by meet name."))
        .in(query[Int]("limit").default(100).description("Limit the number of results returned."))
        .out(jsonBody[ApiSuccessResponseWithData[List[Meet]]].description("Response containing a list of meets matching the filters."))

      val findMeetServerEndpoint = findMeetEndpoint.serverLogic(accountId => (federation, date, meetCountry, meetName, limit) =>
        routes.responseWithData(for {
          _ <- routes.mustHaveAtLeastOne(federation, date, meetCountry, meetName)
          meets <- meetService.findMeet(federation, date, meetCountry, meetName, limit, xa)
        } yield meets)
      )

      List(findMeetServerEndpoint)
    }
  }
}
