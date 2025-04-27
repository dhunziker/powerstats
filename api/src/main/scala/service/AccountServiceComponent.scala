package ai.powerstats.api
package service

import service.util.Base62Helper

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.AccountRepositoryComponent
import ai.powerstats.common.db.model.Account
import ai.powerstats.common.db.model.AccountStatus.{Provisional, Verified}
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.{IO, Resource}
import doobie.Transactor
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.Authorization
import org.http4s.{BasicCredentials, Request}
import org.typelevel.log4cats.LoggerFactory
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import java.time.Clock
import scala.concurrent.duration.DurationInt

trait AccountServiceComponent {
  this: ConfigComponent &
    LoggingComponent &
    ClockComponent &
    HashingServiceComponent &
    EmailServiceComponent &
    AccountRepositoryComponent =>
  val accountService: AccountService

  trait AccountService {
    private val BCryptCost = 6
    private val LoginSessionTimeout = 24.hours
    private val logger = LoggerFactory[IO].getLogger

    def register(email: String, password: String, xa: Transactor[IO]): IO[Account] = {
      for {
        existingUser <- accountRepository.findAccount(email, xa)
        account <- existingUser.map(account => handleExistingAccount(account))
          .getOrElse(handleNewAccount(email, password, xa))
      } yield account
    }

    def activate(activationKey: String, xa: Transactor[IO]): IO[(Account, String)] = {
      for {
        claim <- validateWebToken(activationKey)
        subject <- IO.fromOption(claim.subject)(new Error("Subject not found"))
        accountId <- IO(subject.toLong)
        account <- accountRepository.updateAccount(accountId, xa, status = Some(Verified))
        webToken <- issueWebToken(account.id)
        _ <- logger.info(s"Verified user with email ${account.email}")
      } yield (account, webToken)
    }

    def login(email: String, password: String, xa: Transactor[IO]): IO[String] = {
      for {
        existingUser <- accountRepository.findAccount(email, xa)
        account <- IO.fromOption(existingUser)(new Error(s"Account with email $email not found"))
        verified <- hashingService.verify(password, account.passwordHash)
        _ <- if (verified) {
          logger.info(s"User with email $email authenticated")
        } else {
          logger.info(s"Failed to authenticate user with email $email")
        }
        _ <- IO.raiseUnless(verified)(new Error("Invalid password"))
        webToken <- issueWebToken(account.id)
      } yield webToken
    }

    def validateWebToken(webToken: String)(implicit clock: Clock): IO[JwtClaim] = {
      for {
        apiConfig <- config.appConfig.map(_.api)
        decodedToken = Base62Helper.decodeString(webToken)
        claim <- IO.fromTry(JwtCirce.decode(decodedToken, apiConfig.jwtKey, Seq(JwtAlgorithm.HS512)))
        _ <- IO.raiseUnless(claim.isValid)(new Error("Invalid token"))
      } yield claim
    }

    private def handleExistingAccount(account: Account): IO[Account] = {
      for {
        _ <- IO.raiseUnless(account.status == Provisional) {
          new Error(s"User with email ${account.email} already exists")
        }
        _ <- sendActivationEmail(account)
      } yield account
    }

    private def handleNewAccount(email: String, password: String, xa: Transactor[IO]): IO[Account] = {
      for {
        hashedPassword <- hashingService.hash(password)
        account <- accountRepository.insertAccount(email, hashedPassword, xa)
        _ <- sendActivationEmail(account)
        _ <- logger.info(s"Registered new user with email $email")
      } yield account
    }

    private def sendActivationEmail(account: Account): IO[Unit] = {
      for {
        activationLink <- createActivationLink(account)
        mailjetConfig <- config.mailjetConfig
        response <- createApiClient(mailjetConfig.apiKey, mailjetConfig.secretKey).use { client =>
          emailService.sendActivationEmail(client, 6918140, account.email, account.email, activationLink)
        }
        _ <- IO.raiseUnless(response.messages.head.status == "success") {
          new Error(s"Failed to send activation email to ${account.email}")
        }
      } yield ()
    }

    private def createActivationLink(account: Account): IO[String] = {
      for {
        uiConfig <- config.uiConfig
        webToken <- issueWebToken(account.id)
        activationLink = s"${uiConfig.baseUrl}/user/activate/$webToken"
      } yield activationLink
    }

    private def issueWebToken(accountId: Long)(implicit clock: Clock): IO[String] = for {
      apiConfig <- config.appConfig.map(_.api)
      claim <- IO(JwtClaim()
        .about(accountId.toString)
        .expiresIn(LoginSessionTimeout.toSeconds)
        .issuedNow)
      encoded <- IO(JwtCirce.encode(claim, apiConfig.jwtKey, JwtAlgorithm.HS512))
    } yield Base62Helper.encodeString(encoded)

    private def createApiClient(apiKey: String, apiSecret: String): Resource[IO, Client[IO]] = {
      EmberClientBuilder.default[IO].build.map { client =>
        Client[IO] { (req: Request[IO]) =>
          val authenticatedRequest = req.putHeaders(
            Authorization(BasicCredentials(apiKey, apiSecret))
          )
          client.run(authenticatedRequest)
        }
      }
    }
  }
}
