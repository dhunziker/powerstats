package dev.powerstats.api
package route

import route.request.ApiSuccessResponseWithData
import service.{LifterServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.Lifter
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{path, *}

import java.time.LocalDate

trait LifterRoutesComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    LifterServiceComponent =>
  val lifterRoutes: LifterRoutes

  trait LifterRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val findLiftersEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter")
        .in(query[String]("namePattern"))
        .in(query[Int]("limit").default(100))
        .out(jsonBody[ApiSuccessResponseWithData[List[String]]])

      val findLiftersServerEndpoint = findLiftersEndpoint.serverLogic(accountId => (namePattern, limit) =>
        routes.responseWithData(for {
          lifters <- lifterService.findLifters(namePattern, limit, xa)
        } yield lifters)
      )

      val findLifterEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("v1" / "lifter" / path[String]("name"))
        .out(jsonBody[ApiSuccessResponseWithData[Lifter]])

      val findLifterServerEndpoint = findLifterEndpoint.serverLogic(accountId => name =>
        routes.responseWithData(for {
          lifters <- lifterService.findLifter(name, xa)
        } yield lifters)
      )

      List(findLiftersServerEndpoint, findLifterServerEndpoint)
    }
  }
}
