package ai.powerstats.api
package route

import error.NotFoundError
import route.request.ApiSuccessResponseWithData
import service.EventServiceComponent

import ai.powerstats.common.db.model.Event
import cats.effect.*
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import org.http4s.dsl.io.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

trait EventRoutesComponent {
  this: RoutesComponent &
    EventServiceComponent =>
  val eventRoutes: EventRoutes

  trait EventRoutes {

    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findEventsEndpoint = routes.secureEndpoint.get
        .in("api" / "v1" / "events" / "name" / path[String]("name"))
        .out(jsonBody[ApiSuccessResponseWithData[List[Event]]])

      val findEventsServerEndpoint = findEventsEndpoint.serverLogic(accountId => name =>
        routes.responseWithData(for {
          events <- eventService.findEvents(name, xa)
          _ <- IO.raiseWhen(events.isEmpty)(new NotFoundError(s"No events found for name $name"))
        } yield events)
      )

      List(findEventsServerEndpoint)
    }
  }
}
