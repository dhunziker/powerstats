package ai.powerstats.api
package test

import ai.powerstats.common.db.EventRepositoryComponent
import ai.powerstats.common.model.Event
import cats.effect.IO
import doobie.Transactor
import fs2.io.file.{Files, Path}
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.dsl.io.*

trait MockEventRepositoryComponent extends EventRepositoryComponent {
  val eventRepository: EventRepository = new MockEventRepository {}

  trait MockEventRepository extends EventRepository {
    override def selectEvent(name: String, xa: Transactor[IO]): IO[List[Event]] = {
      val resource = getClass.getResource("/NonEmptyResponse.json")
      Files[IO].readAll(Path(resource.getPath))
        .through(fs2.text.utf8.decode)
        .compile
        .foldMonoid
        .map(parser.decode[List[Event]])
        .flatMap {
          case Left(error) => IO.raiseError(error)
          case Right(events) => IO.pure(events.filter(_.name == name))
        }
    }
  }
}
