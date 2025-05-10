package dev.powerstats.api
package route

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
        .in(query[Option[String]]("federation"))
        .in(query[Option[LocalDate]]("date"))
        .in(query[Option[String]]("meetCountry"))
        .in(query[Option[String]]("meetName"))
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[Meet]]])

      val findMeetServerEndpoint = findMeetEndpoint.serverLogic(accountId => queryParams =>
        routes.responseWithData(for {
          _ <- routes.mustHaveAtLeastOne(queryParams)
          (federation, date, meetCountry, meetName, limit) = queryParams
          meets <- meetService.findMeet(federation, date, meetCountry, meetName, limit, xa)
        } yield meets)
      )

      List(findMeetServerEndpoint)
    }
  }
}
