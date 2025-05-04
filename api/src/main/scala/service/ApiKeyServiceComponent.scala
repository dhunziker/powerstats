package dev.powerstats.api
package service

import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import dev.powerstats.common.db.ApiKeyRepositoryComponent
import dev.powerstats.common.db.model.ApiKey
import dev.powerstats.common.logging.LoggingComponent
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

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
        _ <- logger.info(s"Found ${results.length} API keys for account with ID $accountId")
      } yield results
    }

    def createApiKey(accountId: Long, name: String, xa: Transactor[IO]): IO[(String, ApiKey)] = {
      for {
        publicKey <- IO.pure(randomHex256())
        secretKey <- IO.pure(randomHex256())
        secretKeyHash <- hashingService.hash(secretKey)
        creationDate = LocalDateTime.now()
        expiryDate = creationDate.plusYears(DefaultExpiryTimeInYears)
        apiKey <- apiKeyRepository.insertApiKey(accountId, name, publicKey, secretKeyHash, creationDate, expiryDate, xa)
        _ <- logger.info(s"API key created successfully")
      } yield (secretKey, apiKey)
    }

    private def randomHex256(): String = {
      val arr = Array[Byte](32)
      scala.util.Random.nextBytes(arr)
      arr.iterator.map(b => String.format("%02x", Byte.box(b))).mkString("")
    }

    def deleteApiKey(id: Long, accountId: Long, xa: Transactor[IO]): IO[Unit] = {
      for {
        count <- apiKeyRepository.deleteApiKey(id, accountId, xa)
        _ <- IO.raiseUnless(count >= 1)(new Error("Failed to delete API key, please try again later"))
        _ <- logger.info(s"API key with ID $id deleted successfully")
      } yield ()
    }

    private def hash(apiKey: String): Array[Byte] = {
      BCrypt.withDefaults().hash(6, apiKey.getBytes(StandardCharsets.UTF_8))
    }
  }
}
