package ai.powerstats.api
package route

import test.MockHealthRepositoryComponent

import cats.data.Kleisli
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.*
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthRoutesComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "HealthRoutes"

  it should "respond with a 503 when the database query fails" in withFixture { routes =>
    routes.run(
      Request(uri = Uri.unsafeFromString("/health-check"))
    ).asserting { response =>
      response.status shouldBe Status.ServiceUnavailable
      getBodyText(response) shouldBe "Health check failed"
    }
  }

  it should "respond with a 200 when the database query succeeds" in withFixture { routes =>
    routes.run(
      Request(uri = Uri.unsafeFromString("/health-check"))
    ).asserting { response =>
      response.status shouldBe Status.Ok
      getBodyText(response) shouldBe "Ok"
    }
  }

  private val returnValues = List(false, true).iterator

  trait Fixture extends HealthRoutesComponent with MockHealthRepositoryComponent {
    type T = HealthRoutes
    override val healthRoutes: T = new HealthRoutes {}
    override val checkHealthResponse = returnValues
  }

  private def withFixture(testCode: Kleisli[IO, Request[IO], Response[IO]] => IO[Assertion]) = {
    val fixture = new Fixture {}
    val routes = fixture.healthRoutes.routes(null).orNotFound
    testCode(routes)
  }

  private def getBodyText(response: Response[IO]): String = {
    response.as[String].unsafeRunSync()
  }
}
