package dev.powerstats.api
package test

import cats.effect.IO
import dev.powerstats.common.db.HealthRepositoryComponent
import doobie.Transactor

trait MockHealthRepositoryComponent extends HealthRepositoryComponent {
  val healthRepository: HealthRepository
  val checkHealthResponse: Iterator[Boolean]

  trait MockHealthRepository extends HealthRepository {
    override def checkHealth(xa: Transactor[IO]): IO[Boolean] = IO.pure(checkHealthResponse.next())
  }
}
