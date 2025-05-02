package dev.powerstats.api
package service


import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO

import java.nio.charset.StandardCharsets

trait HashingServiceComponent {
  val hashingService: HashingService

  trait HashingService {
    private val BCryptCost = 6

    def hash(secret: String): IO[Array[Byte]] = IO {
      BCrypt.withDefaults().hash(BCryptCost, secret.getBytes(StandardCharsets.UTF_8))
    }

    def verify(secret: String, hash: Array[Byte]): IO[Boolean] = for {
      result <- IO(BCrypt.verifyer().verify(secret.getBytes(StandardCharsets.UTF_8), hash))
      _ <- IO.raiseUnless(result.validFormat)(new IllegalArgumentException(result.formatErrorMessage))
    } yield result.verified
  }
}
