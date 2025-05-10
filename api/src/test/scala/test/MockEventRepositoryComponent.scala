package dev.powerstats.api
package test

import cats.effect.IO
import dev.powerstats.common.db.EventRepositoryComponent
import dev.powerstats.common.db.model.Event
import doobie.Transactor
import fs2.io.file.{Files, Path}
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.dsl.io.*

import java.time.LocalDate

trait MockEventRepositoryComponent extends EventRepositoryComponent {
  val eventRepository: EventRepository

  trait MockEventRepository extends EventRepository {
    override def findEvents(name: Option[String],
                            sex: Option[String],
                            event: Option[String],
                            equipment: Option[String],
                            federation: Option[String],
                            date: Option[LocalDate],
                            meetCountry: Option[String],
                            meetName: Option[String],
                            limit: Int,
                            xa: Transactor[IO]): IO[List[Event]] = {
      val resource = getClass.getResource("/NonEmptyResponse.json")
      Files[IO].readAll(Path(resource.getPath))
        .through(fs2.text.utf8.decode)
        .compile
        .foldMonoid
        .map(parser.decode[List[Event]])
        .flatMap {
          case Left(error) => IO.raiseError(error)
          case Right(events) => IO.pure(events.filter(_.name == name.get))
        }
    }
  }
}
