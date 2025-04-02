package ai.powerstats.api
package service

import ai.powerstats.common.db.AccountRepositoryComponent
import ai.powerstats.common.logging.LoggingComponent
import ai.powerstats.common.model.Account
import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory

import java.nio.charset.StandardCharsets

trait AccountServiceComponent {
  this: LoggingComponent & AccountRepositoryComponent =>
  val accountService: AccountService

  trait AccountService {
    private val BCryptCost = 6
    private val logger = LoggerFactory[IO].getLogger

    def register(email: String, password: String, xa: Transactor[IO]) = {
      for {
        existingUser <- accountRepository.selectAccount(email, xa)
        _ <- IO.raiseUnless(existingUser.isEmpty) {
          new Error(s"User with email $email already exists")
        }
        hashedPassword <- hash(password)
        _ <- accountRepository.insertAccount(email, hashedPassword, xa).flatMap { count =>
          IO.raiseUnless(count >= 1) {
            new Error("Failed to register user, please try again later")
          }
        }
        _ <- logger.info(s"Registered new user with email $email")
      } yield ()
    }

    def auth(email: String, password: String, xa: Transactor[IO]) = for {
      passwordHash <- accountRepository.selectAccount(email, xa).flatMap {
        case Some(account) => IO.pure(account.password_hash)
        case None => IO.raiseError(new Error(s"Account with email $email not found"))
      }
      verified <- verify(password, passwordHash)
      _ <- if (verified) {
        logger.info(s"User with email $email authenticated")
      } else {
        logger.info(s"Failed to authenticate user with email $email")
      }
      _ <- IO.raiseUnless(verified) {
        new Error("Invalid password")
      }
    } yield ()

    private def hash(password: String): IO[Array[Byte]] = IO {
      BCrypt.withDefaults().hash(BCryptCost, password.getBytes(StandardCharsets.UTF_8))
    }

    private def verify(password: String, hash: Array[Byte]): IO[Boolean] = for {
      result <- IO(BCrypt.verifyer().verify(password.getBytes(StandardCharsets.UTF_8), hash))
      verified <- if (!result.validFormat) IO.raiseError(IllegalArgumentException(result.formatErrorMessage))
      else IO.pure(result.verified)
    } yield verified
  }
}
