package ai.powerstats.api

import Routes.helloWorldRoutes

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp {

  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(helloWorldRoutes)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
