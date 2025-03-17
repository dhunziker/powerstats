package ai.powerstats.api

import Routes.helloWorldRoutes
import db.DbMigrationComponent

import ai.powerstats.common.config.SettingsComponent
import cats.effect.*
import org.http4s.ember.server.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp
  with SettingsComponent
  with DbMigrationComponent {

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  override val config = new Settings {}
  override val migration = new Migration {}

  def run(args: List[String]) = for {
    appConfig <- config.appConfig
    api = appConfig.api
    db = appConfig.database
    _ <- migration.migrate(db.url, db.user, db.password)
    server <- EmberServerBuilder
      .default[IO]
      .withHost(api.host)
      .withPort(api.port)
      .withHttpApp(helloWorldRoutes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  } yield server
}
