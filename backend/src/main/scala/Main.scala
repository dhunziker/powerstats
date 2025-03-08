package ai.powerstats.backend

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run =
    for {
      _ <- IO.println(s"Hello backend!")
    } yield ()
}