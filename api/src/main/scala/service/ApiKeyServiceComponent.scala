package ai.powerstats.api
package service

import ai.powerstats.common.db.ApiKeyRepositoryComponent
import ai.powerstats.common.db.model.ApiKey
import ai.powerstats.common.logging.LoggingComponent
import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.UUID

trait ApiKeyServiceComponent {
  this: LoggingComponent &
    HashingServiceComponent &
    ApiKeyRepositoryComponent =>
  val apiKeyService: ApiKeyService

  trait ApiKeyService {
    private val DefaultExpiryTimeInYears = 3
    private val logger = LoggerFactory[IO].getLogger

    def findApiKeys(accountId: Long, xa: Transactor[IO]): IO[List[ApiKey]] = {
      for {
        results <- apiKeyRepository.findApiKeys(accountId, xa)
        _ <- logger.info(s"Found ${results.length} API keys for user with ID $accountId")
      } yield results
    }

    def createApiKey(accountId: Long, name: String, xa: Transactor[IO]): IO[String] = {
      for {
        key <- IO.pure(UUID.randomUUID().toString)
        keyHash <- hashingService.hash(key)
        creationDate = LocalDateTime.now()
        expiryDate = creationDate.plusYears(DefaultExpiryTimeInYears)
        count <- apiKeyRepository.insertApiKey(accountId, name, keyHash, creationDate, expiryDate, xa)
        _ <- IO.raiseUnless(count >= 1)(new Error("Failed to create API key, please try again later"))
        _ <- logger.info(s"API key created successfully")
      } yield key
    }

    def deleteApiKey(id: Long, userId: Long, xa: Transactor[IO]): IO[Unit] = {
      for {
        count <- apiKeyRepository.deleteApiKey(id, userId, xa)
        _ <- IO.raiseUnless(count >= 1)(new Error("Failed to delete API key, please try again later"))
        _ <- logger.info(s"API key with ID $id deleted successfully")
      } yield ()
    }

    private def hash(apiKey: String): Array[Byte] = {
      BCrypt.withDefaults().hash(6, apiKey.getBytes(StandardCharsets.UTF_8))
    }
  }
}
