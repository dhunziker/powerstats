package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.EventRepositoryComponent
import dev.powerstats.common.db.model.Event
import doobie.Transactor

trait EventServiceComponent {
  this: EventRepositoryComponent =>
  val eventService: EventService

  trait EventService {
    def findEvents(name: String, xa: Transactor[IO]): IO[List[Event]] = {
      eventRepository.findEvents(name, xa)
    }
  }
}
