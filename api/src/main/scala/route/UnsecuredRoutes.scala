package ai.powerstats.api
package route

import cats.effect.IO
import doobie.Transactor
import org.http4s.HttpRoutes
import org.http4s.dsl.io.Root

trait UnsecuredRoutes {
  def routes(xa: Transactor[IO]): HttpRoutes[IO]
}
