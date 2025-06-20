package dev.powerstats.common
package db

import db.model.Event

import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDate

trait EventRepositoryComponent {
  val eventRepository: EventRepository

  trait EventRepository {
    private val columns =
      s"""
        name,
        sex,
        event,
        equipment,
        age,
        age_class,
        birth_year_class,
        division,
        bodyweight_kg,
        weight_class_kg,
        squat_1_kg,
        squat_2_kg,
        squat_3_kg,
        squat_4_kg,
        best_3_squat_kg,
        bench_1_kg,
        bench_2_kg,
        bench_3_kg,
        bench_4_kg,
        best_3_bench_kg,
        deadlift_1_kg,
        deadlift_2_kg,
        deadlift_3_kg,
        deadlift_4_Kg,
        best_3_deadlift_kg,
        total_kg,
        place,
        dots,
        wilks,
        glossbrenner,
        goodlift,
        tested,
        country,
        state,
        federation,
        parent_federation,
        date,
        meet_country,
        meet_state,
        meet_town,
        meet_name,
        sanctioned
      """

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
      val baseQuery = s"select $columns from vw_event"
      (Fragment.const(baseQuery) ++
        Fragments.whereAndOpt(
          name.map(v => fr"name = $v"),
          sex.map(v => fr"sex = $v"),
          event.map(v => fr"event = $v"),
          equipment.map(v => fr"equipment = $v"),
          federation.map(v => fr"federation = $v"),
          date.map(v => fr"date = $v"),
          meetCountry.map(v => fr"meet_country = $v"),
          meetName.map(v => fr"meet_name = $v")
        ) ++ fr"order by date desc limit $limit")
        .query[Event]
        .to[List]
        .transact(xa)
    }

    def truncateEvent(xa: Transactor[IO]): IO[Int] = {
      sql"truncate table event restart identity"
        .update
        .run
        .transact(xa)
    }

    def refreshEventView(xa: Transactor[IO]): IO[Int] = {
      sql"refresh materialized view vw_event with data"
        .update
        .run
        .transact(xa)
    }

    def insertEventBatch(events: List[Event], xa: Transactor[IO]): Int = {
      val sql = s"insert into event ($columns) values (${List.fill(42)("?").mkString(", ")})"
      Update[Event](sql)
        .updateMany(events)
        .transact(xa)
        .unsafeRunSync()
    }
  }
}
