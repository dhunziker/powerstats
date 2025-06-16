package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.LifterRepositoryComponent
import dev.powerstats.common.db.model.Lifter
import doobie.Transactor

import java.time.LocalDate

trait LifterServiceComponent {
  this: LifterRepositoryComponent =>
  val lifterService: LifterService

  trait LifterService {
    def findLifter(namePattern: String,
                   limit: Int,
                   xa: Transactor[IO]): IO[List[Lifter]] = {
      lifterRepository.findLifters(namePattern, limit, xa)
    }
  }
}
