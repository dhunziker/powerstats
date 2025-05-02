package dev.powerstats.api
package service

import test.TestHelper

import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.logging.LoggingComponent
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{Deferred, IO}
import org.http4s.*
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class EmailServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "sendActivationEmail"

  it should "not throw any exceptions when message is sent successfully" in withFixture { emailService =>
    val combined = for {
      deferred <- Deferred[IO, String]
      requestTrap = TestHelper.createRequestTrap(deferred)
      client = TestHelper.createClient("EmailSendSuccessResponse", requestTrap)
      response <- emailService.sendActivationEmail(client, 1, "passenger1@mailjet.com", "passenger 1", "http://localhost:8080/user/activate/1")
      expectedRequest <- TestHelper.loadFromJsonAsString("EmailSendSuccessRequest")
      actualRequest <- deferred.get
    } yield (expectedRequest, actualRequest, response)

    combined.asserting { (expectedRequest, actualRequest, response) =>
      actualRequest should be(expectedRequest)
      response.messages should have size 1
      response.messages.head.status should be("success")
      response.messages.head.to should have size 1
      response.messages.head.to.head.messageId should be(1152921534432145581L)
    }
  }

  trait Fixture extends LoggingComponent
    with ConfigComponent
    with EmailServiceComponent {
    type T = EmailService
    override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override val config = new Config {}
    override val emailService: T = new EmailService {}
  }

  private def withFixture(testCode: Fixture#T => IO[Assertion]) = {
    val fixture = new Fixture {}
    testCode(fixture.emailService)
  }
}
