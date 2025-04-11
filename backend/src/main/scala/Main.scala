package ai.powerstats.backend

import download.DownloaderComponent

import ai.powerstats.common.config.{ConfigComponent, Database}
import ai.powerstats.common.db.model.Event
import ai.powerstats.common.db.{DatabaseTransactorComponent, EventRepositoryComponent}
import cats.effect.{IO, IOApp}
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.nio.file.{Files, Path}
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.jdk.StreamConverters.*

object Main extends IOApp.Simple
  with ConfigComponent
  with DatabaseTransactorComponent
  with EventRepositoryComponent
  with DownloaderComponent {
  override val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  override val config = new Config {}
  override val databaseTransactor = new DatabaseTransactor {}
  override val eventRepository = new EventRepository {}
  override val downloader = new Downloader {}

  private val OpenIpf = "https://openpowerlifting.gitlab.io/opl-csv/files/openipf-latest.zip"

  val run = {
    val appConfig = config.appConfig
    val dbConfig = appConfig.map(_.database)
    val transactor = databaseTransactor.init(dbConfig)
    transactor.use { xa =>
      for {
        isTruncated <- eventRepository.truncateEvent(xa)
        _ <- IO(println(s"Truncated table with result: $isTruncated"))
        tempDir <- IO(Files.createTempDirectory("download"))
        file <- downloader.download(OpenIpf, tempDir)
        _ <- Zip.unzip(file)
        csvFile <- findCsv(tempDir)
        header <- parseHeader(csvFile)
        counts <- processBatches(csvFile, header, dbConfig, xa)
        _ <- IO(println(s"Inserted ${counts.sum} rows"))
        isRefreshed <- eventRepository.refreshEventView(xa)
        _ <- IO(println(s"Refreshed view with result: $isRefreshed"))
      } yield ()
    }
  }

  private def findCsv(path: Path): IO[Path] = {
    IO.fromOption(collectFiles(path).find(_.getFileName.toString.endsWith(".csv")))
      (new RuntimeException(s"No csv files found in $path"))
  }

  private def collectFiles(path: Path): LazyList[Path] = {
    Files.list(path).toScala(LazyList).flatMap { path =>
      if (Files.isDirectory(path)) collectFiles(path)
      else if (Files.isRegularFile(path)) List(path)
      else Nil
    }
  }

  private def parseHeader(csvFile: Path): IO[Map[String, Int]] = {
    IO.fromOption(Files.lines(csvFile).toScala(LazyList)
        .headOption.map(_.split(","))
        .map(_.zipWithIndex.toMap))
      (new RuntimeException(s"No data found in $csvFile"))
  }

  private def processBatches(csvFile: Path, header: Map[String, Int], database: IO[Database], xa: Transactor[IO]): IO[List[Int]] = {
    val parser = parseCsv(header)
    for {
      dbConfig <- database
      batches <- IO(Files.lines(csvFile)
        .skip(1)
        .limit(dbConfig.writeLimit)
        .toScala(LazyList)
        .grouped(dbConfig.batchSize))
      results = batches.map { lines =>
        val parsedEvents = lines.map(parser).toList
        eventRepository.insertEventBatch(parsedEvents, xa)
      }
    } yield results.toList
  }

  private def parseCsv(header: Map[String, Int])(line: String): Event = {
    val tokens = line.split(",")
    Event(
      name = Option(tokens(header("Name"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing Name column")),
      sex = Option(tokens(header("Sex"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing Sex column")),
      event = Option(tokens(header("Event"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing Event column")),
      equipment = Option(tokens(header("Equipment"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing Equipment column")),
      age = Option(tokens(header("Age"))).filter(_.nonEmpty).map(_.toFloat),
      ageClass = Option(tokens(header("AgeClass"))).filter(_.nonEmpty),
      birthYearClass = Option(tokens(header("BirthYearClass"))).filter(_.nonEmpty),
      division = Option(tokens(header("Division"))).filter(_.nonEmpty),
      bodyweightKg = Option(tokens(header("BodyweightKg"))).filter(_.nonEmpty).map(_.toFloat),
      weightClassKg = Option(tokens(header("WeightClassKg"))).filter(_.nonEmpty),
      squat1Kg = Option(tokens(header("Squat1Kg"))).filter(_.nonEmpty).map(_.toFloat),
      squat2Kg = Option(tokens(header("Squat2Kg"))).filter(_.nonEmpty).map(_.toFloat),
      squat3Kg = Option(tokens(header("Squat3Kg"))).filter(_.nonEmpty).map(_.toFloat),
      squat4Kg = Option(tokens(header("Squat4Kg"))).filter(_.nonEmpty).map(_.toFloat),
      best3SquatKg = Option(tokens(header("Best3SquatKg"))).filter(_.nonEmpty).map(_.toFloat),
      bench1Kg = Option(tokens(header("Bench1Kg"))).filter(_.nonEmpty).map(_.toFloat),
      bench2Kg = Option(tokens(header("Bench2Kg"))).filter(_.nonEmpty).map(_.toFloat),
      bench3Kg = Option(tokens(header("Bench3Kg"))).filter(_.nonEmpty).map(_.toFloat),
      bench4Kg = Option(tokens(header("Bench4Kg"))).filter(_.nonEmpty).map(_.toFloat),
      best3BenchKg = Option(tokens(header("Best3BenchKg"))).filter(_.nonEmpty).map(_.toFloat),
      deadlift1Kg = Option(tokens(header("Deadlift1Kg"))).filter(_.nonEmpty).map(_.toFloat),
      deadlift2Kg = Option(tokens(header("Deadlift2Kg"))).filter(_.nonEmpty).map(_.toFloat),
      deadlift3Kg = Option(tokens(header("Deadlift3Kg"))).filter(_.nonEmpty).map(_.toFloat),
      deadlift4Kg = Option(tokens(header("Deadlift4Kg"))).filter(_.nonEmpty).map(_.toFloat),
      best3DeadliftKg = Option(tokens(header("Best3DeadliftKg"))).filter(_.nonEmpty).map(_.toFloat),
      totalKg = Option(tokens(header("TotalKg"))).filter(_.nonEmpty).map(_.toFloat),
      place = Option(tokens(header("Place"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing Place column")),
      dots = Option(tokens(header("Dots"))).filter(_.nonEmpty).map(_.toFloat),
      wilks = Option(tokens(header("Wilks"))).filter(_.nonEmpty).map(_.toFloat),
      glossbrenner = Option(tokens(header("Glossbrenner"))).filter(_.nonEmpty).map(_.toFloat),
      goodlift = Option(tokens(header("Goodlift"))).filter(_.nonEmpty).map(_.toFloat),
      tested = Option(tokens(header("Tested"))).map(_ == "Yes"),
      country = Option(tokens(header("Country"))).filter(_.nonEmpty),
      state = Option(tokens(header("State"))).filter(_.nonEmpty),
      federation = Option(tokens(header("Federation"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing Federation column")),
      parentFederation = Option(tokens(header("ParentFederation"))).filter(_.nonEmpty),
      date = Option(tokens(header("Date"))).filter(_.nonEmpty).map(LocalDate.parse(_, ISO_LOCAL_DATE)).getOrElse(throw new RuntimeException("Missing Date column")),
      meetCountry = Option(tokens(header("MeetCountry"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing MeetCountry column")),
      meetState = Option(tokens(header("MeetState"))).filter(_.nonEmpty),
      meetTown = Option(tokens(header("MeetTown"))).filter(_.nonEmpty),
      meetName = Option(tokens(header("MeetName"))).filter(_.nonEmpty).getOrElse(throw new RuntimeException("Missing MeetName column")),
      sanctioned = Option(tokens(header("Sanctioned"))).map(_ == "Yes")
    )
  }
}