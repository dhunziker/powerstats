package dev.powerstats.api
package route

import error.NotFoundError
import route.request.{ApiSuccessResponseWithData, MeetSearchRequest}
import service.{MeetServiceComponent, SecurityServiceComponent}

import cats.effect.*
import dev.powerstats.common.db.model.Meet
import doobie.*
import io.circe.Encoder
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

trait MeetRoutesComponent {
  this: RoutesComponent &
    SecurityServiceComponent &
    MeetServiceComponent =>
  val meetRoutes: MeetRoutes

  trait MeetRoutes {
    def endpoints(xa: Transactor[IO]): List[ServerEndpoint[Any, IO]] = {
      val meetsByFederationEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("api" / "v1" / "meet" / "federation" / path[String]("federation"))
        .out(jsonBody[ApiSuccessResponseWithData[List[Meet]]])

      val meetsByFederationServerEndpoint = meetsByFederationEndpoint.serverLogic(accountId => federation =>
        routes.responseWithData(for {
          meets <- meetService.findMeet(Some(federation), None, None, xa)
          _ <- IO.raiseWhen(meets.isEmpty)(new NotFoundError(s"No meets found for federation $federation"))
        } yield meets)
      )

      val meetsByCountryEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("api" / "v1" / "meet" / "country" / path[String]("country"))
        .out(jsonBody[ApiSuccessResponseWithData[List[Meet]]])

      val meetsByCountryServerEndpoint = meetsByCountryEndpoint.serverLogic(accountId => country =>
        routes.responseWithData(for {
          meets <- meetService.findMeet(None, Some(country), None, xa)
          _ <- IO.raiseWhen(meets.isEmpty)(new NotFoundError(s"No meets found for country $country"))
        } yield meets)
      )

      val meetsByNameEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("api" / "v1" / "meet" / "name" / path[String]("name"))
        .out(jsonBody[ApiSuccessResponseWithData[List[Meet]]])

      val meetsByNameServerEndpoint = meetsByNameEndpoint.serverLogic(accountId => name =>
        routes.responseWithData(for {
          meets <- meetService.findMeet(None, None, Some(name), xa)
          _ <- IO.raiseWhen(meets.isEmpty)(new NotFoundError(s"No meets found for name $name"))
        } yield meets)
      )

      val meetsSearchEndpoint = routes.secureEndpoint(securityService, xa).get
        .in("api" / "v1" / "meet")
        .in(jsonBody[MeetSearchRequest])
        .out(jsonBody[ApiSuccessResponseWithData[List[Meet]]])

      val meetsSearchServerEndpoint = meetsSearchEndpoint.serverLogic(accountId => searchRequest =>
        routes.responseWithData(meetService.findMeet(searchRequest.federation, searchRequest.meetCountry, searchRequest.meetName, xa))
      )

      List(meetsByFederationServerEndpoint, meetsByCountryServerEndpoint, meetsByNameServerEndpoint, meetsSearchServerEndpoint)
    }
  }
}
