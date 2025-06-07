package dev.powerstats.api
package route

import cats.effect.IO
import doobie.Transactor

import java.time.Clock

trait Authenticator {
  def authenticate(webToken: Option[String], apiKey: Option[String], xa: Transactor[IO])(implicit clock: Clock = Clock.systemDefaultZone()): IO[Long] = {
    (webToken, apiKey) match {
      case (Some(webToken), _) => checkRequestLimit(authenticateWebToken(webToken), 100, xa)
      case (_, Some(apiKey)) => checkRequestLimit(authenticateApiKey(apiKey, xa), 10, xa)
      case (None, None) => IO.raiseError(new Error("Authorization header not found"))
    }
  }

  protected def authenticateWebToken(webToken: String)(implicit clock: Clock = Clock.systemDefaultZone()): IO[Long]

  protected def authenticateApiKey(apiKey: String, xa: Transactor[IO]): IO[Long]

  protected def checkRequestLimit(accountId: IO[Long], limit: Int, xa: Transactor[IO]): IO[Long]
}
