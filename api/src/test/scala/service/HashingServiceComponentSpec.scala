package ai.powerstats.api
package service

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.OptionValues.*
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pdi.jwt.exceptions.JwtLengthException

import java.nio.file.{Files, Path}
import java.time.{Clock, Instant, ZoneId, ZoneOffset}
import scala.concurrent.duration.DurationInt
import scala.jdk.DurationConverters.*

class HashingServiceComponentSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "hash"

  it should "create a secure hash as an array of bytes" in withFixture { hashingService =>
    hashingService.hash("myspace1").asserting { hash =>
      hash should not be empty
    }
  }

  behavior of "verify"

  it should "verify a valid hash successfully" in withFixture { hashingService =>
    hashingService.hash("myspace1")
      .flatMap(hashingService.verify("myspace1", _)).asserting { result =>
        result shouldBe true
      }
  }

  it should "throw an exception when the hash is empty" in withFixture { hashingService =>
    hashingService.verify("myspace1", Array.empty)
      .assertThrowsWithMessage[IllegalArgumentException]("must provide non-null, non-empty hash")
  }

  it should "throw an exception when the hash is invalid" in withFixture { hashingService =>
    hashingService.verify("myspace1", "test".getBytes)
      .assertThrowsWithMessage[IllegalArgumentException](
        "hash prefix meta must be at least 7 bytes long e.g. '$2a$10$' - " +
          "example of expected hash format: '$2a$06$If6bvum7DFjUnE9p2uDeDu0YHzrHM6tf.iqN8.yx.jNN1ILEf7h0i' " +
          "which includes 16 bytes salt and 23 bytes hash value encoded in a base64 flavor"
      )
  }

  trait Fixture extends HashingServiceComponent {
    type T = HashingService
    override val hashingService: T = new HashingService {}
  }

  private def withFixture(testCode: Fixture#T => IO[Assertion]) = {
    val fixture = new Fixture {}
    testCode(fixture.hashingService)
  }
}
