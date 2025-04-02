package ai.powerstats.api
package route

import cats.effect.IO
import doobie.Transactor
import org.http4s.HttpRoutes
import org.http4s.dsl.io.Root

trait Routes {
  val Internal = Root
  val ExternalV1 = Root / "api" / "v1"

  def routes(xa: Transactor[IO]): HttpRoutes[IO]
}
