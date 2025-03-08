package ai.powerstats.api

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object Routes {
  val helloWorldRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Service.hello(name))
  }.orNotFound
}
