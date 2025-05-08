package dev.powerstats.common
package db

import cats.effect.*
import doobie.*
import doobie.implicits.*

trait HealthRepositoryComponent {
  val healthRepository: HealthRepository

  trait HealthRepository {
    def checkHealth(xa: Transactor[IO]): IO[Boolean] = {
      sql"select 1"
        .query[Int]
        .option
        .transact(xa)
        .map(_.isDefined)
    }
  }
}
