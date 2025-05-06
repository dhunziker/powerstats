package dev.powerstats.api
package service

import test.{MockApiKeyRepositoryComponent, TestHelper}

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.db.ApiKeyRepositoryComponent
import dev.powerstats.common.logging.LoggingComponent
import org.scalatest.Assertion
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pdi.jwt.exceptions.{JwtExpirationException, JwtLengthException}

import java.nio.file.{Files, Path}
import java.time.{Clock, Instant, ZoneId, ZoneOffset}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.DurationConverters.*

class SecurityServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "validateWebToken"

  it should "return the claim for a valid web token" in withFixture(23.hours) { f =>
    import f.*
    (for {
      token <- TestHelper.loadFromFile("web_token_valid")
      claim <- securityService.validateWebToken(token)
    } yield claim).asserting { claim =>
      claim.isValid should be(true)
      claim.subject.value should be("0")
    }
  }

  it should "throw an exception when the token is invalid" in withFixture(23.hours) { f =>
    import f.*
    securityService.validateWebToken("").assertThrows[JwtLengthException]
  }

  it should "throw an exception when the token is expired" in withFixture(24.hours) { f =>
    import f.*
    (for {
      token <- TestHelper.loadFromFile("web_token_valid")
      _ <- securityService.validateWebToken(token)
    } yield ()).assertThrowsWithMessage[JwtExpirationException]("The token is expired since 2025-04-28T10:00:00Z")
  }

  behavior of "hash"

  it should "create a secure hash as an array of bytes" in withFixture { securityService =>
    securityService.hashSecret("myspace1").asserting { hash =>
      hash should not be empty
    }
  }

  behavior of "validateHashedSecret"

  it should "verify a valid hash successfully" in withFixture { securityService =>
    securityService.hashSecret("myspace1")
      .flatMap(securityService.validateHashedSecret("myspace1", _)).asserting { result =>
        result shouldBe true
      }
  }

  it should "throw an exception when the hash is empty" in withFixture { securityService =>
    securityService.validateHashedSecret("myspace1", Array.empty)
      .assertThrowsWithMessage[IllegalArgumentException]("must provide non-null, non-empty hash")
  }

  it should "throw an exception when the hash is invalid" in withFixture { securityService =>
    securityService.validateHashedSecret("myspace1", "test".getBytes)
      .assertThrowsWithMessage[IllegalArgumentException](
        "hash prefix meta must be at least 7 bytes long e.g. '$2a$10$' - " +
          "example of expected hash format: '$2a$06$If6bvum7DFjUnE9p2uDeDu0YHzrHM6tf.iqN8.yx.jNN1ILEf7h0i' " +
          "which includes 16 bytes salt and 23 bytes hash value encoded in a base64 flavor"
      )
  }

  trait Fixture extends SecurityServiceComponent
    with ConfigComponent
    with MockApiKeyRepositoryComponent {
    type T = SecurityService
    override val config = new Config {}
    override val apiKeyRepository = new MockApiKeyRepository {}
    override val securityService: T = new SecurityService {}
    protected val baseClock: Clock = Clock.fixed(Instant.parse("2025-04-27T10:00:00.00Z"), ZoneOffset.UTC)
    implicit val clock: Clock = baseClock
  }

  private def withFixture(testCode: Fixture#T => IO[Assertion]) = {
    val fixture = new Fixture {}
    testCode(fixture.securityService)
  }

  private def withFixture(offset: FiniteDuration)(testCode: Fixture => IO[Assertion]): IO[Assertion] = {
    val fixture = new Fixture {
      override implicit val clock: Clock = Clock.offset(baseClock, offset.toJava)
    }
    testCode(fixture)
  }
}
