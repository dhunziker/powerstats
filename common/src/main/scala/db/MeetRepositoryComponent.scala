package dev.powerstats.common
package db

import db.model.Meet

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDate

trait MeetRepositoryComponent {
  val meetRepository: MeetRepository

  trait MeetRepository {
    def findMeets(federation: Option[String],
                  date: Option[LocalDate],
                  meetCountry: Option[String],
                  meetName: Option[String],
                  limit: Int,
                  xa: Transactor[IO]): IO[List[Meet]] = {
      val baseQuery =
        fr"""
          select
            federation,
            date,
            meet_country,
            meet_state,
            meet_town,
            meet_name
          from vw_meet
        """
      (baseQuery ++
        Fragments.whereAndOpt(
          federation.map(v => fr"federation = $v"),
          date.map(v => fr"date = $v"),
          meetCountry.map(v => fr"meet_country = $v"),
          meetName.map(v => fr"meet_name = $v")
        ) ++ fr"limit $limit")
        .query[Meet]
        .to[List]
        .transact(xa)
    }

    def refreshMeetView(xa: Transactor[IO]): IO[Int] = {
      sql"refresh materialized view vw_meet with data"
        .update
        .run
        .transact(xa)
    }
  }
}
