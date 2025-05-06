package dev.powerstats.api
package service

import route.Authenticator
import service.util.Base64Helper

import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.db.{AccountRepositoryComponent, ApiKeyRepositoryComponent}
import dev.powerstats.common.logging.LoggingComponent
import doobie.Transactor
import org.typelevel.log4cats.LoggerFactory
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import java.nio.charset.StandardCharsets
import java.time.Clock
import scala.concurrent.duration.DurationInt

trait SecurityServiceComponent {
  this: ConfigComponent & ApiKeyRepositoryComponent =>
  val securityService: SecurityService

  trait SecurityService extends Authenticator {
    private val BCryptCost = 6
    private val LoginSessionTimeout = 24.hours
    private val charset = StandardCharsets.UTF_8

    override protected def authenticateWebToken(webToken: String)(implicit clock: Clock = Clock.systemDefaultZone()) = for {
      claim <- validateWebToken(webToken)
      subject <- IO.fromOption(claim.subject)(new Error("Subject not found"))
      accountId <- IO.pure(subject.toLong).adaptError(t => new Error(t.getMessage))
    } yield accountId

    override protected def authenticateApiKey(apiKey: String, xa: Transactor[IO]) = for {
      decodedApiKey <- IO(Base64Helper.decodeString(apiKey))
      apiKeyTokens = decodedApiKey.split(":")
      (publicKey, secretKey) = (apiKeyTokens(0), apiKeyTokens(1))
      apiKey <- apiKeyRepository.findApiKeys(publicKey, xa)
      verified <- validateHashedSecret(secretKey, apiKey.secretKeyHash)
      _ <- IO.raiseUnless(verified)(new Error("Invalid secret key"))
    } yield apiKey.accountId

    def issueWebToken(accountId: Long)(implicit clock: Clock = Clock.systemDefaultZone()): IO[String] = for {
      apiConfig <- config.apiConfig
      claim <- IO(JwtClaim()
        .about(accountId.toString)
        .expiresIn(LoginSessionTimeout.toSeconds)
        .issuedNow)
      encoded <- IO(JwtCirce.encode(claim, apiConfig.jwtKey, JwtAlgorithm.HS512))
    } yield Base64Helper.encodeString(encoded)

    def validateWebToken(webToken: String)(implicit clock: Clock = Clock.systemDefaultZone()): IO[JwtClaim] = {
      for {
        apiConfig <- config.apiConfig
        decodedToken = Base64Helper.decodeString(webToken)
        claim <- IO.fromTry(JwtCirce(clock).decode(decodedToken, apiConfig.jwtKey, Seq(JwtAlgorithm.HS512)))
      } yield claim
    }

    def hashSecret(secret: String): IO[Array[Byte]] = IO {
      BCrypt.withDefaults().hash(BCryptCost, secret.getBytes(charset))
    }

    def validateHashedSecret(secret: String, hash: Array[Byte]): IO[Boolean] = for {
      result <- IO(BCrypt.verifyer().verify(secret.getBytes(charset), hash))
      _ <- IO.raiseUnless(result.validFormat)(new IllegalArgumentException(result.formatErrorMessage))
    } yield result.verified
  }
}
