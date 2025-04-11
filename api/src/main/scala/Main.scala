package ai.powerstats.api

import db.DatabaseMigrationComponent
import route.{AccountRoutesComponent, EventRoutesComponent, HealthRoutesComponent}
import service.{AccountServiceComponent, EventServiceComponent}

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.{AccountRepositoryComponent, DatabaseTransactorComponent, EventRepositoryComponent, HealthRepositoryComponent}
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.*
import cats.implicits.toSemigroupKOps
import org.http4s.ember.server.*
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp.Simple
  with ConfigComponent
  with LoggingComponent
  with DatabaseMigrationComponent
  with DatabaseTransactorComponent
  with HealthRepositoryComponent
  with HealthRoutesComponent
  with EventRepositoryComponent
  with EventServiceComponent
  with EventRoutesComponent
  with AccountRepositoryComponent
  with AccountServiceComponent
  with AccountRoutesComponent {
  override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  override val config = new Config {}
  override val databaseMigration = new DatabaseMigration {}
  override val databaseTransactor = new DatabaseTransactor {}
  override val healthRepository = new HealthRepository {}
  override val healthRoutes = new HealthRoutes {}
  override val eventRepository = new EventRepository {}
  override val eventService = new EventService {}
  override val eventRoutes = new EventRoutes {}
  override val accountRepository = new AccountRepository {}
  override val accountService = new AccountService {}
  override val accountRoutes = new AccountRoutes {}

  val run = {
    val appConfig = config.appConfig
    val dbConfig = appConfig.map(_.database)
    val transactor = databaseTransactor.init(dbConfig)
    transactor.use { xa =>
      for {
        apiConfig <- appConfig.map(_.api)
        _ <- databaseMigration.migrate(dbConfig)
        routes = (eventRoutes.routes(xa) <+>
          healthRoutes.routes(xa) <+>
          accountRoutes.routes(xa)
          ).orNotFound
        corsService <- CORS.policy.withAllowOriginAll(routes)
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(apiConfig.host)
          .withPort(apiConfig.port)
          .withHttpApp(corsService)
          .build
          .use(_ => IO.never)
          .as(ExitCode.Success)
      } yield ()
    }
  }
}
