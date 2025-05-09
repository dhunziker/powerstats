package dev.powerstats.common
package db

import db.model.Meet

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

trait MeetRepositoryComponent {
  val meetRepository: MeetRepository

  trait MeetRepository {
    def findMeets(federation: Option[String], meetCountry: Option[String], meetName: Option[String], xa: Transactor[IO]): IO[List[Meet]] = {
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
      val federationFilter = federation.map(v => fr"federation = $v")
      val meetCountryFilter = meetCountry.map(v => fr"meet_country = $v")
      val meetNameFilter = meetName.map(f => fr"meet_name = $f")
      (baseQuery ++ Fragments.whereAndOpt(federationFilter, meetCountryFilter, meetNameFilter))
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
