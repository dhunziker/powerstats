package ai.powerstats.api
package service

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.db.AccountRepositoryComponent
import ai.powerstats.common.db.model.AccountStatus.Verified
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.IO
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import java.time.Clock
import scala.concurrent.duration.DurationInt

trait AccountServiceComponent {
  this: ConfigComponent &
    LoggingComponent &
    HashingServiceComponent &
    AccountRepositoryComponent =>
  val accountService: AccountService

  trait AccountService {
    private val BCryptCost = 6
    private val LoginSessionTimeout = 24.hours
    private val logger = LoggerFactory[IO].getLogger

    def register(email: String, password: String, xa: Transactor[IO]): IO[Unit] = {
      for {
        existingUser <- accountRepository.findAccount(email, xa)
        _ <- IO.raiseUnless(existingUser.isEmpty)(new Error(s"User with email $email already exists"))
        hashedPassword <- hashingService.hash(password)
        count <- accountRepository.insertAccount(email, hashedPassword, xa)
        _ <- IO.raiseUnless(count >= 1)(new Error("Failed to register user, please try again later"))
        _ <- logger.info(s"Registered new user with email $email")
      } yield ()
    }

    def activate(activationKey: String, xa: Transactor[IO]): IO[Unit] = {
      for {
        claim <- validateWebToken(activationKey)(Clock.systemDefaultZone())
        subject <- IO.fromOption(claim.subject)(new Error("Subject not found"))
        accountId <- IO(subject.toLong)
        count <- accountRepository.updateAccount(accountId, Verified, xa)
        _ <- IO.raiseUnless(count >= 1)(new Error("Failed to update user, please try again later")) 
        _ <- logger.info(s"Verified user with account ID $accountId")
      } yield ()
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
        webToken <- issueWebToken(account.id)(Clock.systemDefaultZone())
      } yield webToken
    }

    def validateWebToken(token: String)(implicit clock: Clock): IO[JwtClaim] = for {
      apiConfig <- config.appConfig.map(_.api)
      claim <- IO.fromTry(JwtCirce.decode(token, apiConfig.jwtKey, Seq(JwtAlgorithm.HS512)))
      _ <- IO.raiseUnless(claim.isValid)(new Error("Invalid token"))
    } yield claim

    private def issueWebToken(accountId: Long)(implicit clock: Clock): IO[String] = for {
      apiConfig <- config.appConfig.map(_.api)
      claim <- IO(JwtClaim()
        .about(accountId.toString)
        .expiresIn(LoginSessionTimeout.toSeconds)
        .issuedNow)
      encoded <- IO(JwtCirce.encode(claim, apiConfig.jwtKey, JwtAlgorithm.HS512))
    } yield encoded
  }
}
