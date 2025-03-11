package ai.powerstats.backend

import cats.effect.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Path

class Downloader {

  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val logger = LoggerFactory[IO].getLogger

  def download(url: String, targetDir: Path): IO[Path] = {
    for {
      _ <- logger.info(s"Downloading $url")
      targetFile <- IO(targetDir.resolve(Path.of(url).getFileName))
      _ <- logger.info(s"Downloading to $targetFile")
      _ <- IO(new URI(url).toURL.openStream).bracket { in =>
        for {
          _ <- logger.info(s"${in.available()} bytes available to read")
          _ <- IO(new FileOutputStream(targetFile.toFile)).bracket { out =>
            IO(Iterator.continually(in.read)
              .takeWhile(_ != -1)
              .foreach(out.write))
          } {
            out => IO(out.close())
          }
        } yield ()
      } { in =>
        IO(in.close())
      }
      _ <- logger.info(s"${targetFile.getFileName} download complete")
    } yield targetFile
  }
}
