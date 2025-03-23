package ai.powerstats.backend
package download

import ai.powerstats.common.logging.LoggingComponent
import cats.effect.*
import fs2.*
import org.typelevel.log4cats.LoggerFactory

import java.io.{FileOutputStream, InputStream, OutputStream}
import java.net.URI
import java.nio.file.Path as JPath

trait DownloaderComponent extends LoggingComponent {
  val downloader: Downloader

  trait Downloader {
    private val ChunkSize = 4096
    private val logger = LoggerFactory[IO].getLogger

    def download(url: String, targetDir: JPath): IO[JPath] = for {
      _ <- logger.info(s"Downloading $url")
      targetPath <- IO(targetDir.resolve(JPath.of(url).getFileName))
      _ <- logger.info(s"Saving to $targetPath")
      source = createInputStream(url)
      target = createOutputStream(targetPath)
      sizeRead <- write(source, target)
        .compile
        .toList
        .map(_.sum)
      _ <- logger.info(s"$sizeRead bytes downloaded")
    } yield targetPath

    private def createInputStream(url: String): IO[InputStream] = {
      IO(new URI(url).toURL
        .openConnection
        .getInputStream)
    }

    private def createOutputStream(targetPath: JPath): IO[OutputStream] = {
      IO(new FileOutputStream(targetPath.toFile))
    }

    private def write(source: IO[InputStream], target: IO[OutputStream]): Stream[IO, Long] = {
      io.readInputStream(source, ChunkSize)
        .chunks
        .flatMap(Stream.chunk)
        .observe(io.writeOutputStream(target))
        .chunks
        .fold(0L)((acc, chunk) => acc + chunk.size)
    }
  }
}
