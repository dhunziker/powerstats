package dev.powerstats.api
package route

import route.request.ApiResponse.given
import route.request.ApiSuccessResponseWithData
import service.{EventServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.Event
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.time.LocalDate

trait EventRoutesComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    EventServiceComponent =>
  val eventRoutes: EventRoutes

  trait EventRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findEventEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "event")
        .in(query[Option[String]]("name"))
        .in(query[Option[String]]("sex"))
        .in(query[Option[String]]("event"))
        .in(query[Option[String]]("equipment"))
        .in(query[Option[String]]("federation"))
        .in(query[Option[LocalDate]]("date"))
        .in(query[Option[String]]("meetCountry"))
        .in(query[Option[String]]("meetName"))
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[Event]]])

      val findEventServerEndpoint = findEventEndpoint.serverLogic(accountId => (name, sex, event, equipment, federation, date, meetCountry, meetName, limit) =>
        routes.responseWithData(for {
          _ <- routes.mustHaveAtLeastOne(name, sex, event, equipment, federation, date, meetCountry, meetName)
          events <- eventService.findEvents(name, sex, event, equipment, federation, date, meetCountry, meetName, limit, xa)
        } yield events)
      )

      List(findEventServerEndpoint)
    }
  }
}
