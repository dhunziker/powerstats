package ai.powerstats.backend

import cats.effect.{IO, IOApp}

import java.nio.file.{Files, Path}
import scala.jdk.StreamConverters.*

object Main extends IOApp.Simple {
  private val OpenIpf = "https://openpowerlifting.gitlab.io/opl-csv/files/openipf-latest.zip"
  private val downloader = new Downloader
  val run =
    for {
      tempDir <- IO(Files.createTempDirectory("download"))
      file <- downloader.download(OpenIpf, tempDir)
      _ <- Zip.unzip(file)
      _ <- printLines(tempDir)
    } yield ()

  private def printLines(tempDir: Path): IO[Unit] = IO {
    val csvFile = findCsv(tempDir)
      .getOrElse(throw new RuntimeException("No csv files found"))
    val lines = Files.lines(csvFile)
    try {
      lines.limit(10).forEach(line => println(line))
    } finally {
      lines.close()
    }
  }

  private def collectFiles(path: Path): LazyList[Path] = {
    Files.list(path).toScala(LazyList).flatMap { path =>
      if (Files.isDirectory(path)) collectFiles(path)
      else if (Files.isRegularFile(path)) List(path)
      else Nil
    }
  }

  private def findCsv(path: Path): Option[Path] = {
    collectFiles(path).find(_.getFileName.toString.endsWith(".csv"))
  }
}