package ai.powerstats.backend
package download

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.nio.file.{Files, Path}
import scala.concurrent.Future

class DownloaderSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  behavior of "A Downloader"

  it should "download a file into a target directory" in withFixture { (downloader, tempDir) =>
    val source = getClass.getResource("/Test.zip")
    downloader.download(source.toString, tempDir).asserting { path =>
      path shouldBe tempDir.resolve("Test.zip")
      path.toFile.exists() shouldBe true
    }
  }

  trait Fixture extends DownloaderComponent {
    type T = Downloader
    override val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    override val downloader: T = new Downloader {}
  }

  private def withFixture(testCode: (Fixture#T, Path) => IO[Assertion]) = withTempDir { tempDir =>
    val fixture = new Fixture {}
    testCode(fixture.downloader, tempDir)
  }

  private def withTempDir(testCode: Path => IO[Assertion]) = {
    for {
      _ <- IO(Files.createTempDirectory("test")).bracket { tempDir =>
        testCode(tempDir)
      } { tempDir =>
        IO(deleteRecursively(tempDir))
      }
    } yield ()
  }

  private def deleteRecursively(path: Path): Unit = {
    if (Files.isDirectory(path)) {
      Files.list(path).forEach(deleteRecursively)
    }
    Files.delete(path)
  }
}
