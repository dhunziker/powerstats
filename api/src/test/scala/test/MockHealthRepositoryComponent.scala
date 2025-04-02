package ai.powerstats.api
package test

import ai.powerstats.common.db.HealthRepositoryComponent
import cats.effect.IO
import doobie.Transactor

trait MockHealthRepositoryComponent extends HealthRepositoryComponent {
  val healthRepository = new MockHealthRepository {}
  val checkHealthResponse: Iterator[Boolean]

  trait MockHealthRepository extends HealthRepository {
    override def checkHealth(xa: Transactor[IO]): IO[Boolean] = IO.pure(checkHealthResponse.next())
  }
}
