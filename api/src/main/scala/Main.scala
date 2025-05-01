package ai.powerstats.api

import db.DatabaseMigrationComponent
import route.*
import service.*

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.*
import ai.powerstats.common.logging.LoggingComponent
import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.toSemigroupKOps
import cats.syntax.all.*
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sttp.apispec.openapi.OpenAPI
import sttp.tapir.*
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.time.Clock

object Main extends IOApp.Simple
  with ConfigComponent
  with LoggingComponent
  with ClockComponent
  with HashingServiceComponent
  with EmailServiceComponent
  with DatabaseMigrationComponent
  with DatabaseTransactorComponent
  with RoutesComponent
  with HealthRepositoryComponent
  with HealthRoutesComponent
  with EventRepositoryComponent
  with EventServiceComponent
  with EventRoutesComponent
  with AccountRepositoryComponent
  with AccountServiceComponent
  with AccountRoutesComponent
  with ApiKeyRepositoryComponent
  with ApiKeyServiceComponent
  with ApiKeyRoutesComponent {
  override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  override implicit val clock: Clock = Clock.systemDefaultZone()

  override val config = new Config {}
  override val hashingService = new HashingService {}
  override val emailService = new EmailService {}
  override val databaseMigration = new DatabaseMigration {}
  override val databaseTransactor = new DatabaseTransactor {}
  override val routes = new Routes {}
  override val healthRepository = new HealthRepository {}
  override val healthRoutes = new HealthRoutes {}
  override val eventRepository = new EventRepository {}
  override val eventService = new EventService {}
  override val eventRoutes = new EventRoutes {}
  override val accountRepository = new AccountRepository {}
  override val accountService = new AccountService {}
  override val accountRoutes = new AccountRoutes {}
  override val apiKeyRepository = new ApiKeyRepository {}
  override val apiKeyService = new ApiKeyService {}
  override val apiKeyRoutes = new ApiKeyRoutes {}

  val run = {
    val appConfig = config.appConfig
    val dbConfig = appConfig.map(_.database)
    val transactor = databaseTransactor.init(dbConfig)
    transactor.use { xa =>
      for {
        apiConfig <- appConfig.map(_.api)
        _ <- databaseMigration.migrate(dbConfig)
        internalApiEndpoints =
          healthRoutes.endpoints(xa) <+>
            accountRoutes.endpoints(xa)
        apiEndpoints =
          eventRoutes.endpoints(xa) <+>
            apiKeyRoutes.endpoints(xa)
        docEndpoints = SwaggerInterpreter(customiseDocsModel = customiseDocsModel)
          .fromServerEndpoints[IO](apiEndpoints, "PowerStats API", "1.0.0")
        serverOptions = Http4sServerOptions.customiseInterceptors[IO].options
        routes = Http4sServerInterpreter[IO](serverOptions)
          .toRoutes(internalApiEndpoints ++ apiEndpoints ++ docEndpoints)
          .orNotFound
        corsService = CORS.policy.withAllowOriginAll(routes)
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

  private def customiseDocsModel(openApi: OpenAPI) = {
    openApi.components.map { components =>
      openApi.components(
        components.securitySchemes(
          components.securitySchemes.filterNot { (schemeName, _) =>
            schemeName == "internal"
          }
        )
      )
    }.getOrElse(openApi)
  }
}
