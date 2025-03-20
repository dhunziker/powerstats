package ai.powerstats.common
package db

import model.Event

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDate

trait EventRepositoryComponent {
  val eventRepository: EventRepository

  trait EventRepository {
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

    def insertEventBatch(events: IO[List[Event]], xa: Transactor[IO]): IO[Int] = {
      val sql: String =
        """INSERT INTO event (
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
            ) VALUES (
              ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 
              ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            )""".stripMargin
      for {
        events <- events
        inserted <- Update[Event](sql)
          .updateMany(events)
          .transact(xa)
      } yield inserted
    }
  }
}
