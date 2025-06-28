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
        .description("Endpoint to retrieve a list of events based on various filters.")
        .in(query[Option[String]]("name").description("Filter by lifter's name."))
        .in(query[Option[String]]("sex").description("Filter by lifter's sex."))
        .in(query[Option[String]]("event").description("Filter by event type."))
        .in(query[Option[String]]("equipment").description("Filter by equipment used."))
        .in(query[Option[String]]("federation").description("Filter by federation name."))
        .in(query[Option[LocalDate]]("date").description("Filter by event date."))
        .in(query[Option[String]]("meetCountry").description("Filter by meet country."))
        .in(query[Option[String]]("meetName").description("Filter by meet name."))
        .in(query[Int]("limit").default(100).description("Limit the number of results returned."))
        .out(jsonBody[ApiSuccessResponseWithData[List[Event]]].description("Response containing a list of events matching the filters."))

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
