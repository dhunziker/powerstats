package dev.powerstats.api

import db.DatabaseMigrationComponent
import route.*
import service.*

import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.toSemigroupKOps
import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.db.*
import dev.powerstats.common.logging.LoggingComponent
import org.http4s.*
import org.http4s.ember.server.*
import org.http4s.headers.*
import org.http4s.implicits.*
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sttp.apispec.openapi.OpenAPI
import sttp.tapir.*
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.time.Clock
import scala.concurrent.duration.*

object Main extends IOApp.Simple
  with LoggingComponent
  with ConfigComponent
  with EmailServiceComponent
  with DatabaseMigrationComponent
  with DatabaseTransactorComponent
  with RoutesComponent
  with SecurityServiceComponent
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

  override val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  override val config = new Config {}
  override val emailService = new EmailService {}
  override val databaseMigration = new DatabaseMigration {}
  override val databaseTransactor = new DatabaseTransactor {}
  override val routes = new Routes {}
  override val securityService = new SecurityService {}
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
    val transactor = databaseTransactor.init(config.dbConfig)
    transactor.use { xa =>
      for {
        _ <- databaseMigration.migrate(config.dbConfig)
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
        corsService = CORS.policy
          .withAllowOriginHost(Set(
            Origin.Host(Uri.Scheme.https, Uri.RegName("powerstats-ui.onrender.com"), None),
            Origin.Host(Uri.Scheme.https, Uri.RegName("www.powerstats.dev"), None),
            Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(9000))
          ))
          .withAllowHeadersAll
          .withAllowMethodsIn(Set(Method.GET, Method.POST, Method.DELETE))
          .withAllowCredentials(false)
          .withMaxAge(1.day)
          .apply(routes)
        apiConfig <- config.apiConfig
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
