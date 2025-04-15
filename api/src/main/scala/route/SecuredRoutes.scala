package ai.powerstats.api
package route

import cats.effect.IO
import doobie.Transactor
import org.http4s.dsl.io.Root
import org.http4s.{AuthedRoutes, HttpRoutes}

trait SecuredRoutes {
  def routes(xa: Transactor[IO]): AuthedRoutes[Long, IO]
}
