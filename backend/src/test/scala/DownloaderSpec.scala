package ai.powerstats.backend

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Assertion, BeforeAndAfterEach}

import java.nio.file.{Files, Path}
import scala.concurrent.Future

class DownloaderSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with BeforeAndAfterEach {

  private def deleteRecursively(path: Path): Unit = {
    if (Files.isDirectory(path)) {
      Files.list(path).forEach(deleteRecursively)
    }
    Files.delete(path)
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

  behavior of "A downloader"

  it should "download and extract files into target directory" in withTempDir { tempDir =>
    val downloader = new Downloader()
    val source = getClass.getResource("/Test.zip")
    downloader.download(source.toString, tempDir).asserting { path =>
      path shouldBe tempDir.resolve("Test.zip")
    }
  }
}
