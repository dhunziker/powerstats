package dev.powerstats.api
package service

import cats.effect.IO
import dev.powerstats.common.db.ApiKeyRepositoryComponent
import dev.powerstats.common.db.model.ApiKey
import dev.powerstats.common.logging.LoggingComponent
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory

import java.time.LocalDateTime

trait ApiKeyServiceComponent {
  this: LoggingComponent &
    SecurityServiceComponent &
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
        publicKey <- IO.pure(randomHex(32))
        secretKey <- IO.pure(randomHex(32))
        secretKeyHash <- securityService.hashSecret(secretKey)
        creationDate = LocalDateTime.now()
        expiryDate = creationDate.plusYears(DefaultExpiryTimeInYears)
        apiKey <- apiKeyRepository.insertApiKey(accountId, name, publicKey, secretKeyHash, creationDate, expiryDate, xa)
        _ <- logger.info(s"API key created successfully")
      } yield (secretKey, apiKey)
    }

    def deleteApiKey(id: Long, accountId: Long, xa: Transactor[IO]): IO[Unit] = {
      for {
        count <- apiKeyRepository.deleteApiKey(id, accountId, xa)
        _ <- IO.raiseUnless(count >= 1)(new Error("Failed to delete API key, please try again later"))
        _ <- logger.info(s"API key with ID $id deleted successfully")
      } yield ()
    }


    private def randomHex(length: Int): String = {
      val arr = Array.ofDim[Byte](length / 2)
      scala.util.Random.nextBytes(arr)
      arr.iterator.map(b => String.format("%02x", Byte.box(b))).mkString
    }
  }
}
