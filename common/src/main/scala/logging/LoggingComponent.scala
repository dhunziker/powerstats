package ai.powerstats.common
package logging

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory

trait LoggingComponent {
  this: LoggingComponent =>
  implicit val loggerFactory: LoggerFactory[IO]
}
