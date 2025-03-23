package ai.powerstats.api
package route

import service.EventServiceComponent

import cats.effect.*
import doobie.*
import doobie.implicits.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*

trait EventRoutesComponent {
  this: EventServiceComponent =>
  val eventRoutes: EventRoutes

  trait EventRoutes {
    def routes(xa: Transactor[IO]) = {
      HttpRoutes.of[IO] {
        case GET -> Root / "api" / "v1" / "events" / "name" / name =>
          for {
            events <- eventService.findEvents(name, xa)
            response <- {
              if (events.isEmpty) NotFound()
              else Ok(events.asJson)
            }
          } yield response

        case GET -> Root / "health-check" =>
          sql"select 1"
            .query[Int]
            .unique
            .transact(xa)
            .attempt
            .flatMap {
              case Left(value) =>
                IO.raiseError(value)
              case Right(value) =>
                Ok("Ok")
            }
      }.orNotFound
    }
  }
}
