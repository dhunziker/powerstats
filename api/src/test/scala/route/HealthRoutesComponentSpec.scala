package dev.powerstats.api
package route

import Main.{AccountService, Routes}
import service.*
import test.*

import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.logging.LoggingComponent
import cats.data.Kleisli
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.*
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}

import java.time
import java.time.Clock

class HealthRoutesComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "HealthRoutes"

  it should "respond with a 503 when the database query fails" in withFixture { routes =>
    (for {
      response <- routes.run(Request(uri = Uri.unsafeFromString("/health-check")))
      expectedResponse <- TestHelper.loadFromJsonAsString("HealthCheckErrorResponse")
    } yield (response, expectedResponse)).asserting { (response, expectedResponse) =>
      response.status shouldBe Status.ServiceUnavailable
      getBodyText(response) should be(expectedResponse)
    }
  }

  it should "respond with a 200 when the database query succeeds" in withFixture { routes =>
    (for {
      response <- routes.run(Request(uri = Uri.unsafeFromString("/health-check")))
      expectedResponse <- TestHelper.loadFromJsonAsString("HealthCheckSuccessResponse")
    } yield (response, expectedResponse)).asserting { (response, expectedResponse) =>
      response.status shouldBe Status.Ok
      getBodyText(response) should be(expectedResponse)
    }
  }

  private val returnValues = List(false, true).iterator

  trait Fixture extends HealthRoutesComponent
    with RoutesComponent
    with AccountServiceComponent
    with ConfigComponent
    with LoggingComponent
    with ClockComponent
    with HashingServiceComponent
    with MockEmailServiceComponent
    with MockAccountRepositoryComponent
    with MockHealthRepositoryComponent {
    type T = HealthRoutes
    override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override implicit val clock: Clock = Clock.systemDefaultZone()
    override val config = new Config {}
    override val hashingService = new HashingService {}
    override val accountRepository = new MockAccountRepository {}
    override val emailService: MockEmailService = new MockEmailService {}
    override val accountService = new AccountService {}
    override val routes = new Routes {}
    override val healthRoutes: T = new HealthRoutes {}
    override val checkHealthResponse = returnValues
  }

  private def withFixture(testCode: Kleisli[IO, Request[IO], Response[IO]] => IO[Assertion]) = {
    val fixture = new Fixture {}
    val serverOptions = Http4sServerOptions.customiseInterceptors[IO].options
    val routes = Http4sServerInterpreter[IO](serverOptions).toRoutes(fixture.healthRoutes.endpoints(null)).orNotFound
    testCode(routes)
  }

  private def getBodyText(response: Response[IO]): String = {
    response.as[String].unsafeRunSync()
  }
}
