package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.EventRepositoryComponent
import dev.powerstats.common.db.model.Event
import doobie.Transactor

import java.time.LocalDate

trait EventServiceComponent {
  this: EventRepositoryComponent =>
  val eventService: EventService

  trait EventService {
    def findEvents(name: Option[String],
                   sex: Option[String],
                   event: Option[String],
                   equipment: Option[String],
                   federation: Option[String],
                   date: Option[LocalDate],
                   meetCountry: Option[String],
                   meetName: Option[String],
                   limit: Int,
                   xa: Transactor[IO]): IO[List[Event]] = {
      eventRepository.findEvents(name, sex, event, equipment, federation, date, meetCountry, meetName, limit, xa)
    }
  }
}
