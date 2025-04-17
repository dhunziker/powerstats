package ai.powerstats.api
package service

import Main.HashingService
import test.MockAccountRepositoryComponent

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pdi.jwt.exceptions.JwtLengthException

import java.nio.file.{Files, Path}
import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.duration.DurationInt
import scala.jdk.DurationConverters.*

class AccountServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "register"

  it should "throw an exception when the email already exists" in withFixture { accountService =>
    accountService.register("my.account@gmail.com", "", null)
      .assertThrowsWithMessage[Error]("User with email my.account@gmail.com already exists")
  }

  it should "throw an exception when the account cannot be created" in withFixture { accountService =>
    accountService.register("my.new.account@gmail.com", "", null)
      .assertThrowsWithMessage[Error]("Failed to register user, please try again later")
  }

  it should "insert a new account successfully" in withFixture { accountService =>
    accountService.register("my.new.account@gmail.com", "", null).assertNoException
  }

  behavior of "login"

  it should "throw an exception when user is not found" in withFixture { accountService =>
    accountService.login("your.account@gmail.com", "", null)
      .assertThrowsWithMessage[Error]("Account with email your.account@gmail.com not found")
  }

  it should "throw an exception when the hash format is invalid" in withFixture { accountService =>
    accountService.login("my.invalid.account@gmail.com", "", null)
      .assertThrowsWithMessage[IllegalArgumentException]("must provide non-null, non-empty hash")
  }

  it should "throw an exception when the password is incorrect" in withFixture { accountService =>
    accountService.login("my.account@gmail.com", "myspace2", null)
      .assertThrowsWithMessage[Error]("Invalid password")
  }

  it should "return a web token when the correct credentials are provided" in withFixture { accountService =>
    accountService.login("my.account@gmail.com", "myspace1", null).asserting { token =>
      token should not be empty
    }
  }

  behavior of "validateWebToken"

  it should "return a valid web token" in withFixture { accountService =>
    val token = Files.readString(Path.of(getClass.getResource("/valid_key.txt").toURI)).stripLineEnd
    val baseClock = Clock.fixed(Instant.parse("2025-04-17T08:00:00.00Z"), ZoneOffset.UTC)
    implicit val clock: Clock = Clock.offset(baseClock, 23.hours.toJava)
    accountService.validateWebToken(token).asserting { result =>
      result.isValid shouldBe true
      result.subject.value shouldBe "1"
    }
  }

  it should "throw an exception when the token is invalid" in withFixture { accountService =>
    val baseClock = Clock.fixed(Instant.parse("2025-04-17T08:00:00.00Z"), ZoneOffset.UTC)
    implicit val clock: Clock = Clock.offset(baseClock, 23.hours.toJava)
    accountService.validateWebToken("").assertThrows[JwtLengthException]
  }

  it should "throw an exception when the token is expired" in withFixture { accountService =>
    val token = Files.readString(Path.of(getClass.getResource("/valid_key.txt").toURI)).stripLineEnd
    val baseClock = Clock.fixed(Instant.parse("2025-04-17T08:00:00.00Z"), ZoneOffset.UTC)
    implicit val clock: Clock = Clock.offset(baseClock, 24.hours.toJava)
    accountService.validateWebToken(token)
      .assertThrowsWithMessage[Error]("Invalid token")
  }

  private val insertReturnValues = List(0, 1).iterator

  trait Fixture extends AccountServiceComponent
    with MockAccountRepositoryComponent
    with LoggingComponent
    with ConfigComponent
    with HashingServiceComponent {
    type T = AccountService
    override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override val config = new Config {}
    override val hashingService = new HashingService {}
    override val accountService: T = new AccountService {}
    override val insertCounts = insertReturnValues
    override val accountRepository = new MockAccountRepository {}
  }

  private def withFixture(testCode: Fixture#T => IO[Assertion]) = {
    val fixture = new Fixture {}
    testCode(fixture.accountService)
  }
}
