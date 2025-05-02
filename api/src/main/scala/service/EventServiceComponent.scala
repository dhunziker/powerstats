package dev.powerstats.api
package service

import dev.powerstats.common.db.EventRepositoryComponent
import dev.powerstats.common.db.model.Event
import cats.effect.IO
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
