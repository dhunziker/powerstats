package ai.powerstats.api
package service

import test.MockAccountRepositoryComponent

import ai.powerstats.common.logging.LoggingComponent
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class AccountServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "An AccountService"

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

  it should "throw an exception when user is not found" in withFixture { accountService =>
    accountService.auth("your.account@gmail.com", "", null)
      .assertThrowsWithMessage[Error]("Account with email your.account@gmail.com not found")
  }

  it should "throw an exception when the hash format is invalid" in withFixture { accountService =>
    accountService.auth("my.invalid.account@gmail.com", "", null)
      .assertThrowsWithMessage[IllegalArgumentException]("must provide non-null, non-empty hash")
  }

  it should "throw an exception when the password is incorrect" in withFixture { accountService =>
    accountService.auth("my.account@gmail.com", "myspace2", null)
      .assertThrowsWithMessage[Error]("Invalid password")
  }

  it should "not fail when the correct password is provided" in withFixture { accountService =>
    accountService.auth("my.account@gmail.com", "myspace1", null).assertNoException
  }

  private val insertReturnValues = List(0, 1).iterator
  private val updateReturnValues = List(0, 1).iterator

  trait Fixture extends AccountServiceComponent with MockAccountRepositoryComponent with LoggingComponent {
    type T = AccountService
    override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override val accountService: T = new AccountService {}
    override val insertCounts = insertReturnValues
    override val updateCounts = updateReturnValues
    override val accountRepository = new MockAccountRepository {}
  }

  private def withFixture(testCode: Fixture#T => IO[Assertion]) = {
    val fixture = new Fixture {}
    testCode(fixture.accountService)
  }
}
