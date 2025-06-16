package dev.powerstats.common
package db

import db.model.Lifter

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDate

trait LifterRepositoryComponent {
  val lifterRepository: LifterRepository

  trait LifterRepository {
    def findLifters(namePattern: String,
                    limit: Int,
                    xa: Transactor[IO]): IO[List[Lifter]] = {
      sql"""
        select
          name,
          sex,
          equipment,
          best_3_squat_kg,
          best_3_bench_kg,
          best_3_deadlift_kg,
          total_kg,
          dots,
          wilks,
          glossbrenner,
          goodlift
        from vw_lifter
        where name ~* $namePattern
        limit $limit
      """
        .query[Lifter]
        .to[List]
        .transact(xa)
    }

    def refreshLifterView(xa: Transactor[IO]): IO[Int] = {
      sql"refresh materialized view vw_lifter with data"
        .update
        .run
        .transact(xa)
    }
  }
}
