package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.model.{Event, PersonalBest}
import dev.powerstats.common.db.{EventRepositoryComponent, LifterRepositoryComponent}
import doobie.Transactor

import java.time.LocalDate

trait LifterServiceComponent {
  this: LifterRepositoryComponent &
    EventRepositoryComponent =>
  val lifterService: LifterService

  trait LifterService {
    def findLifters(namePattern: String,
                    limit: Int,
                    xa: Transactor[IO]): IO[List[String]] = {
      lifterRepository.findLifters(namePattern, limit, xa)
    }

    def findPersonalBest(name: String, xa: Transactor[IO]): IO[List[PersonalBest]] = {
      lifterRepository.findPersonalBests(name, xa)
    }

    def findCompetitionResults(name: String, limit: Int, xa: Transactor[IO]): IO[List[Event]] = {
      eventRepository.findEvents(name = Some(name), None, None, None, None, None, None, None, limit, xa)
    }
  }
}
