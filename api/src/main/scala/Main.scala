package ai.powerstats.api

import db.DatabaseMigrationComponent
import route.EventRoutesComponent
import service.EventServiceComponent

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.{DatabaseTransactorComponent, EventRepositoryComponent}
import cats.effect.*
import org.http4s.ember.server.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp.Simple
  with ConfigComponent
  with DatabaseMigrationComponent
  with DatabaseTransactorComponent
  with EventRepositoryComponent
  with EventServiceComponent
  with EventRoutesComponent {
  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  override val config = new Config {}
  override val databaseMigration = new DatabaseMigration {}
  override val databaseTransactor = new DatabaseTransactor {}
  override val eventRepository = new EventRepository {}
  override val eventService = new EventService {}
  override val eventRoutes = new EventRoutes {}

  val run = {
    val appConfig = config.appConfig
    val dbConfig = appConfig.map(_.database)
    val transactor = databaseTransactor.init(dbConfig)
    transactor.use { xa =>
      for {
        apiConfig <- appConfig.map(_.api)
        _ <- databaseMigration.migrate(dbConfig)
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(apiConfig.host)
          .withPort(apiConfig.port)
          .withHttpApp(eventRoutes.routes(xa))
          .build
          .use(_ => IO.never)
          .as(ExitCode.Success)
      } yield ()
    }
  }
}
