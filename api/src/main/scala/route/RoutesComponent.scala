package dev.powerstats.api
package route

import error.{NotFoundError, ServiceUnavailableError}
import route.request.{ApiError, ApiErrorResponse, ApiSuccessResponse, ApiSuccessResponseWithData}

import cats.effect.*
import cats.syntax.all.*
import doobie.util.transactor.Transactor
import io.circe.Decoder
import io.circe.generic.auto.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.EndpointInput.Auth.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.jsonBody

trait RoutesComponent {
  val routes: Routes

  trait Routes {
    given Decoder[StatusCode] = Decoder.decodeInt.map(StatusCode.apply)

    given Schema[StatusCode] = Schema.string[StatusCode]

    val publicEndpoint = endpoint
      .errorOut(statusCode)
      .errorOut(jsonBody[ApiErrorResponse])

    def secureEndpoint(authenticator: Authenticator, xa: Transactor[IO]) = publicEndpoint
      .securityIn(auth.bearer[Option[String]]().securitySchemeName("internal"))
      .securityIn(auth.basic[Option[String]]().bearerFormat("Authorization: Basic <encoded_credentials>"))
      .serverSecurityLogic { case (bearerToken, basicAuth) =>
        handleResponse(authenticator.authenticate(bearerToken, basicAuth, xa), isSecurityLogic = true)
      }

    def mustHaveAtLeastOne(queryParams: Product): IO[Unit] = {
      IO.raiseUnless(queryParams.productIterator.exists(param => param != None))(
        new Error("Please provide at least one query parameter"))
    }

    def response(serverLogic: IO[Unit]): IO[Either[(StatusCode, ApiErrorResponse), ApiSuccessResponse]] = {
      handleResponse(serverLogic.map(_ => ApiSuccessResponse()))
    }

    def responseWithData[A](serverLogic: IO[A], isSecurityLogic: Boolean = false): IO[Either[(StatusCode, ApiErrorResponse), ApiSuccessResponseWithData[A]]] = {
      handleResponse(serverLogic.map(data => ApiSuccessResponseWithData(data)), isSecurityLogic)
    }

    private def handleResponse[A](response: IO[A], isSecurityLogic: Boolean = false): IO[Either[(StatusCode, ApiErrorResponse), A]] = {
      for {
        attempt <- response.attempt
        withStatusCode = attempt.leftMap {
          // TODO: Handle additional types of exceptions here
          case err: NotFoundError => (StatusCode.NotFound, err)
          case err: ServiceUnavailableError => (StatusCode.ServiceUnavailable, err)
          case err if isSecurityLogic => (StatusCode.Unauthorized, err)
          case err => (StatusCode.InternalServerError, err)
        }
        withErrorResponse = withStatusCode.leftMap(handleErrorResponse)
      } yield withErrorResponse
    }

    private def handleErrorResponse(statusCode: StatusCode, throwable: Throwable): (StatusCode, ApiErrorResponse) = {
      (statusCode, ApiErrorResponse(statusCode, ApiError(
        code = throwable.getClass.getSimpleName,
        message = throwable.getMessage
      )))
    }
  }
}
