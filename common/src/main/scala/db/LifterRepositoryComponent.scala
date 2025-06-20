package dev.powerstats.common
package db

import db.model.PersonalBest

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
                    xa: Transactor[IO]): IO[List[String]] = {
      sql"""
        select distinct name
        from vw_personal_best
        where name ~* $namePattern
        limit $limit
      """
        .query[String]
        .to[List]
        .transact(xa)
    }

    def findPersonalBests(name: String, xa: Transactor[IO]): IO[List[PersonalBest]] = {
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
        from vw_personal_best
        where name = $name
      """
        .query[PersonalBest]
        .to[List]
        .transact(xa)
    }

    def refreshLifterView(xa: Transactor[IO]): IO[Int] = {
      sql"refresh materialized view vw_personal_best with data"
        .update
        .run
        .transact(xa)
    }
  }
}
