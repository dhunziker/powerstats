package ai.powerstats.api
package service

import ai.powerstats.common.db.ApiKeyRepositoryComponent
import ai.powerstats.common.db.model.ApiKey
import cats.effect.IO
import doobie.Transactor

import java.time.LocalDateTime
import java.util.UUID

trait ApiKeyServiceComponent {
  this: ApiKeyRepositoryComponent =>
  val apiKeyService: ApiKeyService

  trait ApiKeyService {
    def findApiKey(apiKey: String, xa: Transactor[IO]): IO[List[ApiKey]] = {
      IO(
        List(
          ApiKey(1L, 1L, UUID.randomUUID().toString, LocalDateTime.now(), LocalDateTime.now()),
          ApiKey(2L, 1L, UUID.randomUUID().toString, LocalDateTime.now(), LocalDateTime.now()),
          ApiKey(3L, 1L, UUID.randomUUID().toString, LocalDateTime.now(), LocalDateTime.now())
        )
      )
    }
  }
}
