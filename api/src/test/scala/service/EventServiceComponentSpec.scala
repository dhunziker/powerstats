package ai.powerstats.api
package service

import Main.EventService
import test.MockEventRepositoryComponent

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class EventServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "An EventService"

  it should "return an empty list when name is not found" in withFixture { eventService =>
    eventService.findEvents("Albert Einstein", null).asserting { events =>
      events shouldBe empty
    }
  }

  it should "return all events for a given name" in withFixture { eventService =>
    eventService.findEvents("Dennis Hunziker", null).asserting { events =>
      events should have size 7
      val names = events.map(_.name).distinct
      names should have size 1
      names.head shouldBe "Dennis Hunziker"
    }
  }

  trait Fixture extends EventServiceComponent with MockEventRepositoryComponent {
    type T = EventService
    override val eventService: T = new EventService {}
  }

  private def withFixture(testCode: Fixture#T => IO[Assertion]) = {
    val fixture = new Fixture {}
    testCode(fixture.eventService)
  }
}
