package dev.powerstats.common
package logging

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory

trait LoggingComponent {
  implicit def loggerFactory: LoggerFactory[IO]
}
