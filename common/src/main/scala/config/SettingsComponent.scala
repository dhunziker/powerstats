package ai.powerstats.common
package config

import cats.effect.IO
import io.circe.Decoder
import io.circe.config.parser
import io.circe.derivation.Configuration
import io.circe.generic.auto.*

trait SettingsComponent {
  val config: Settings

  trait Settings {
    def appConfig: IO[AppConfig] = parser.decodePathF[IO, AppConfig]("app")
  }
}
