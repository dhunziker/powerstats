package dev.powerstats.api
package test

import cats.effect.IO
import dev.powerstats.common.db.ApiKeyRepositoryComponent
import dev.powerstats.common.db.model.ApiKey
import doobie.Transactor

import java.time.LocalDateTime

trait MockApiKeyRepositoryComponent extends ApiKeyRepositoryComponent {
  val apiKeyRepository: ApiKeyRepository

  trait MockApiKeyRepository extends ApiKeyRepository with MockRepository[ApiKey] {

    override def findApiKeys(accountId: Long, xa: Transactor[IO]): IO[List[ApiKey]] = {
      filterStorage(apiKey => apiKey.accountId == accountId)
    }

    override def findApiKeys(publicKey: String, xa: Transactor[IO]): IO[ApiKey] = {
      findInStorage(apiKey => apiKey.publicKey == publicKey).map(_.get)
    }

    override def insertApiKey(accountId: Long,
                              name: String,
                              publicKey: String,
                              secretKeyHash: Array[Byte],
                              creationDate: LocalDateTime,
                              expiryDate: LocalDateTime,
                              xa: Transactor[IO]): IO[ApiKey] = {
      for {
        apiKey <- IO.pure(ApiKey(
          nextId,
          accountId,
          name,
          publicKey,
          secretKeyHash,
          creationDate,
          expiryDate))
        _ <- addToStorage(apiKey.id, apiKey)
      } yield apiKey
    }

    override def deleteApiKey(id: Long, accountId: Long, xa: Transactor[IO]): IO[Int] = {
      removeFromStorage(id, apiKey => apiKey.id == id && apiKey.accountId == accountId)
    }
  }
}
