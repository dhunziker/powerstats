package dev.powerstats.common
package config

import config.Api.*

import cats.effect.IO
import io.circe.Decoder
import io.circe.config.parser
import io.circe.derivation.Configuration
import io.circe.generic.auto.*

trait ConfigComponent {
  val config: Config

  trait Config {
    def appConfig: IO[AppConfig] = parser.decodePathF[IO, AppConfig]("app")

    def uiConfig: IO[Ui] = appConfig.map(_.ui)

    def mailjetConfig: IO[Mailjet] = appConfig.map(_.mailjet)
  }
}
