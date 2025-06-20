package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.LifterRepositoryComponent
import dev.powerstats.common.db.model.PersonalBest
import doobie.Transactor

import java.time.LocalDate

trait LifterServiceComponent {
  this: LifterRepositoryComponent =>
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
  }
}
