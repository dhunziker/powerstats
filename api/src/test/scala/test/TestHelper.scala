package ai.powerstats.api
package test

import cats.effect.kernel.Resource
import cats.effect.{Deferred, IO}
import fs2.io.file.{Files, Path}
import fs2.text
import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.{Decoder, Json, parser}
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.uri
import org.scalatest.Assertions.fail

object TestHelper {
  type RequestTrap = Request[IO] => IO[Unit]

  def loadFromJson[T](fileName: String)(implicit decoder: Decoder[T]): IO[T] = {
    val resource = getClass.getResource(s"/$fileName.json")
    Files[IO].readAll(Path(resource.getPath))
      .through(fs2.text.utf8.decode)
      .compile
      .foldMonoid
      .flatMap(content => IO.fromEither(parser.decode[T](content)))
  }

  def loadFromJsonAsString(fileName: String): IO[String] = {
    for {
      content <- loadFromJson[Json](fileName)
    } yield content.noSpaces
  }

  def createClient(fileName: String, requestTrap: RequestTrap): Client[IO] = {
    Client((req: Request[IO]) => {
      for {
        _ <- requestTrap(req).toResource
        resp <- createMockResponse(fileName, Ok)
      } yield resp
    })
  }

  def createRequestTrap(deferred: Deferred[IO, String])(req: Request[IO]): IO[Unit] = {
    for {
      requestBody <- req.entity.body.through(text.utf8.decode).compile.string
      _ <- deferred.complete(requestBody)
    } yield ()
  }

  private def createMockResponse(fileName: String, status: Status): Resource[IO, Response[fs2.Pure]] = {
    loadFromJson[Json](fileName)
      .toResource
      .map(content => Response()
        .withStatus(status)
        .withEntity(content))
  }
}
