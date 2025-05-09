package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.MeetRepositoryComponent
import dev.powerstats.common.db.model.{Event, Meet}
import doobie.Transactor

trait MeetServiceComponent {
  this: MeetRepositoryComponent =>
  val meetService: MeetService

  trait MeetService {
    def findMeet(federation: Option[String], meetCountry: Option[String], meetName: Option[String], xa: Transactor[IO]): IO[List[Meet]] = {
      meetRepository.findMeets(federation, meetCountry, meetName, xa)
    }
  }
}
