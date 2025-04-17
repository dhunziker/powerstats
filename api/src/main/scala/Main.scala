package ai.powerstats.api

import db.DatabaseMigrationComponent
import route.{AccountRoutesComponent, ApiKeyRoutesComponent, EventRoutesComponent, HealthRoutesComponent}
import service.{AccountServiceComponent, ApiKeyServiceComponent, EventServiceComponent, HashingServiceComponent}

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.*
import ai.powerstats.common.db.model.Account
import ai.powerstats.common.logging.LoggingComponent
import cats.*
import cats.data.*
import cats.effect.*
import cats.implicits.{toSemigroupKOps, *}
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import org.http4s.server.*
import org.http4s.server.middleware.CORS
import org.http4s.syntax.header.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.time.Clock

object Main extends IOApp.Simple
  with ConfigComponent
  with LoggingComponent
  with HashingServiceComponent
  with DatabaseMigrationComponent
  with DatabaseTransactorComponent
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

  override val config = new Config {}
  override val hashingService = new HashingService {}
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
  override val apiKeyRepository = new ApiKeyRepository {}
  override val apiKeyService = new ApiKeyService {}
  override val apiKeyRoutes = new ApiKeyRoutes {}

  private val authUser: Kleisli[IO, Request[IO], Either[String, Long]] = Kleisli({ request =>
    val authUser = for {
      token <- IO(request.headers.get[Authorization] match {
        case Some(Authorization(Credentials.Token(AuthScheme.Bearer, token))) => token
        case None => throw new Error("Authorization header not found")
        case _ => throw new Error("Invalid Authorization header")
      })
      claim <- accountService.validateWebToken(token)(Clock.systemDefaultZone())
      subject <- IO.fromOption(claim.subject)(new Error("Subject not found"))
      accountId <- IO(subject.toLong)
    } yield accountId
    authUser
      .attempt
      .map(_.leftMap(_.toString))
  })
  private val onFailure: AuthedRoutes[String, IO] = Kleisli({ request =>
    OptionT.liftF(IO(
      Response[IO](status = Unauthorized)
        .withEntity(request.context)))
  })
  private val authMiddleware: AuthMiddleware[IO, Long] = AuthMiddleware(authUser, onFailure)

  val run = {
    val appConfig = config.appConfig
    val dbConfig = appConfig.map(_.database)
    val transactor = databaseTransactor.init(dbConfig)
    transactor.use { xa =>
      for {
        apiConfig <- appConfig.map(_.api)
        _ <- databaseMigration.migrate(dbConfig)
        routes = (healthRoutes.routes(xa) <+>
          eventRoutes.routes(xa) <+>
          accountRoutes.routes(xa) <+>
          authMiddleware(apiKeyRoutes.routes(xa))).orNotFound
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
