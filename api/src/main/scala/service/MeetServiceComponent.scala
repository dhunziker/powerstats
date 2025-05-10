package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.MeetRepositoryComponent
import dev.powerstats.common.db.model.Meet
import doobie.Transactor

import java.time.LocalDate

trait MeetServiceComponent {
  this: MeetRepositoryComponent =>
  val meetService: MeetService

  trait MeetService {
    def findMeet(federation: Option[String],
                 date: Option[LocalDate],
                 meetCountry: Option[String],
                 meetName: Option[String],
                 limit: Int,
                 xa: Transactor[IO]): IO[List[Meet]] = {
      meetRepository.findMeets(federation, date, meetCountry, meetName, limit, xa)
    }
  }
}
