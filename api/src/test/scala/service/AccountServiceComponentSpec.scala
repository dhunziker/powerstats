package ai.powerstats.api
package service

import Main.HashingService
import test.{MockAccountRepositoryComponent, MockEmailServiceComponent, TestHelper}

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.model.AccountStatus
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pdi.jwt.exceptions.{JwtExpirationException, JwtLengthException, JwtValidationException}

import java.nio.charset.StandardCharsets
import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.duration.*
import scala.jdk.DurationConverters.ScalaDurationOps

class AccountServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "register"

  it should "insert a new account successfully" in withFixture { f =>
    (for {
      account <- f.accountService.register("my.account@gmail.com", "myspace1", null)
      emailSent <- f.emailService.requested()
    } yield (account, emailSent)).asserting { (account, emailSent) =>
      account.id should be(0)
      account.email should be("my.account@gmail.com")
      account.passwordHash should not be empty
      emailSent should be(true)
    }
  }

  it should "send an activation email when a provisional account exists" in withFixture { f =>
    (for {
      inserted <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      account <- f.accountService.register("my.account@gmail.com", "myspace1", null)
      emailSent <- f.emailService.requested()
    } yield (inserted, account, emailSent)).asserting { (inserted, account, emailSent) =>
      account should be(inserted)
      emailSent should be(true)
    }
  }

  it should "throw an exception when a non-provisional account exists" in withFixture { f =>
    (for {
      inserted <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      updated <- f.accountRepository.updateAccount(inserted.id, null, status = Some(AccountStatus.Verified))
      assertError <- f.accountService.register("my.account@gmail.com", "myspace1", null)
        .assertThrowsWithMessage[Error]("Account with email my.account@gmail.com already exists")
      emailSent <- f.emailService.requested()
    } yield (assertError, emailSent)).asserting { (assertError, emailSent) =>
      emailSent should be(false)
      assertError
    }
  }

  behavior of "activate"

  it should "activate account successfully" in withFixture { f =>
    (for {
      inserted <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      activationKey <- TestHelper.loadFromFile("web_token_valid")
      (account, webToken) <- f.accountService.activate(activationKey, null)
    } yield (inserted, account, webToken)).asserting { (inserted, account, webToken) =>
      account.id should be(inserted.id)
      account.status should be(AccountStatus.Verified)
      webToken should not be empty
    }
  }

  it should "throw an exception when token is not valid" in withFixture { f =>
    (for {
      _ <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      activationKey <- TestHelper.loadFromFile("web_token_wrong_secret")
      _ <- f.accountService.activate(activationKey, null)
    } yield ()).assertThrowsWithMessage[JwtValidationException]("Invalid signature for this token or wrong algorithm.")
  }

  it should "throw an exception when subject is not found" in withFixture { f =>
    (for {
      _ <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      activationKey <- TestHelper.loadFromFile("web_token_missing_subject")
      _ <- f.accountService.activate(activationKey, null)
    } yield ()).assertThrowsWithMessage[Error]("Subject not found")
  }

  it should "throw an exception when subject is not an existing account" in withFixture { f =>
    (for {
      _ <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      activationKey <- TestHelper.loadFromFile("web_token_invalid_subject")
      _ <- f.accountService.activate(activationKey, null)
    } yield ()).assertThrowsWithMessage[NoSuchElementException]("key not found: 1")
  }

  behavior of "login"

  it should "return a web token when the correct credentials are provided" in withFixture { f =>
    (for {
      _ <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      token <- f.accountService.login("my.account@gmail.com", "myspace1", null)
    } yield token).asserting { token =>
      token should not be empty
    }
  }

  it should "throw an exception when account is not found" in withFixture { f =>
    f.accountService.login("my.account@gmail.com", "myspace1", null)
      .assertThrowsWithMessage[Error]("Account with email my.account@gmail.com not found")
  }

  it should "throw an exception when the hash format is invalid" in withFixture { f =>
    (for {
      _ <- f.accountRepository.insertAccount("my.account@gmail.com", "".getBytes(StandardCharsets.UTF_8), null)
      _ <- f.accountService.login("my.account@gmail.com", "", null)
    } yield ()).assertThrowsWithMessage[IllegalArgumentException]("must provide non-null, non-empty hash")
  }

  it should "throw an exception when the password is incorrect" in withFixture { f =>
    (for {
      _ <- f.accountRepository.insertAccount("my.account@gmail.com", f.passwordHash, null)
      _ <- f.accountService.login("my.account@gmail.com", "myspace2", null)
    } yield ()).assertThrowsWithMessage[Error]("Invalid password")
  }

  behavior of "validateWebToken"

  it should "return the claim for a valid web token" in withFixture(23.hours) { f =>
    import f.*
    (for {
      token <- TestHelper.loadFromFile("web_token_valid")
      claim <- accountService.validateWebToken(token)
    } yield claim).asserting { claim =>
      claim.isValid should be(true)
      claim.subject.value should be("0")
    }
  }

  it should "throw an exception when the token is invalid" in withFixture(23.hours) { f =>
    import f.*
    accountService.validateWebToken("").assertThrows[JwtLengthException]
  }

  it should "throw an exception when the token is expired" in withFixture(24.hours) { f =>
    import f.*
    (for {
      token <- TestHelper.loadFromFile("web_token_valid")
      _ <- accountService.validateWebToken(token)
    } yield ()).assertThrowsWithMessage[JwtExpirationException]("The token is expired since 2025-04-28T10:00:00Z")
  }

  trait Fixture extends AccountServiceComponent
    with ConfigComponent
    with LoggingComponent
    with ClockComponent
    with HashingServiceComponent
    with MockEmailServiceComponent
    with MockAccountRepositoryComponent {
    type T = AccountService
    override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override val config = new Config {}
    override val hashingService = new HashingService {}
    override val accountRepository = new MockAccountRepository {}
    override val emailService: MockEmailService = new MockEmailService {}
    override val accountService: T = new AccountService {}
    val baseClock = Clock.fixed(Instant.parse("2025-04-27T10:00:00.00Z"), ZoneOffset.UTC)
    val passwordHash = "$2a$06$ujgKk1ts4xNdNVvHClvyWOkTSoy0MlyZNZLMWM059NrKDLsGHOVca".getBytes(StandardCharsets.UTF_8)
  }

  private def withFixture(testCode: Fixture => IO[Assertion]): IO[Assertion] = withFixture(0.seconds)(testCode)

  private def withFixture(offset: FiniteDuration)(testCode: Fixture => IO[Assertion]): IO[Assertion] = {
    val fixture = new Fixture {
      override implicit val clock: Clock = Clock.offset(baseClock, offset.toJava)
    }
    testCode(fixture)
  }
}
