package dev.powerstats.api
package service

import test.MockApiKeyRepositoryComponent

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dev.powerstats.common.db.model.ApiKey
import dev.powerstats.common.logging.LoggingComponent
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.time.LocalDateTime

class ApiKeyServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "findApiKeys"

  it should "find all API keys for a given account" in withFixture { f =>
    (for {
      _ <- f.testApiKeys
      apiKeys <- f.apiKeyService.findApiKeys(1L, null)
    } yield apiKeys).asserting { apiKeys =>
      apiKeys should have size 3
    }
  }

  it should "return empty list if no matching API keys are found" in withFixture { f =>
    (for {
      _ <- f.testApiKeys
      apiKeys <- f.apiKeyService.findApiKeys(3L, null)
    } yield apiKeys).asserting { apiKeys =>
      apiKeys should have size 0
    }
  }

  behavior of "createApiKey"

  it should "create a new API key successfully" in withFixture { f =>
    (for {
      (secretKey, apiKey) <- f.apiKeyService.createApiKey(1L, "Default", null)
      inserted <- f.apiKeyRepository.findApiKeys(1L, null).map(_.head)
    } yield (secretKey, apiKey, inserted)).asserting { (secretKey, apiKey, inserted) =>
      apiKey should be(inserted)
      apiKey.publicKey should have size 32
      secretKey should have size 32
    }
  }

  behavior of "deleteApiKey"

  it should "delete API key successfully" in withFixture { f =>
    (for {
      _ <- f.testApiKeys
      _ <- f.apiKeyService.deleteApiKey(4, 2, null)
    } yield ()).assertNoException
  }

  it should "throw an exception when an API key cannot be deleted" in withFixture { f =>
    (for {
      _ <- f.testApiKeys
      _ <- f.apiKeyService.deleteApiKey(5, 2, null)
    } yield ()).assertThrowsWithMessage[Error]("Failed to delete API key, please try again later")
  }

  trait Fixture extends ApiKeyServiceComponent
    with LoggingComponent
    with HashingServiceComponent
    with MockApiKeyRepositoryComponent {
    type T = ApiKeyService
    override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override val hashingService = new HashingService {}
    override val apiKeyRepository = new MockApiKeyRepository {}
    override val apiKeyService: T = new ApiKeyService {}

    val testApiKeys: IO[List[ApiKey]] = {
      (List.fill(3)(1L) ++ List.fill(2)(2L))
        .map(accountId => apiKeyRepository.insertApiKey(
          accountId,
          "Default",
          "",
          "".getBytes,
          LocalDateTime.now(),
          LocalDateTime.now(),
          null))
        .sequence
    }
  }

  private def withFixture(testCode: Fixture => IO[Assertion]): IO[Assertion] = {
    val fixture = new Fixture {}
    testCode(fixture)
  }
}
