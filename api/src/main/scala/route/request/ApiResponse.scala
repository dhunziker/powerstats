package dev.powerstats.api
package route.request

import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.encodedExample
import sttp.tapir.SchemaType.SInteger

enum ApiStatus {
  case Success, Error
}

sealed trait ApiResponse {
  def status: ApiStatus

  def statusCode: StatusCode
}

object ApiResponse {
  given Decoder[StatusCode] = Decoder.decodeInt.map(StatusCode.apply)

  given Schema[ApiStatus] = Schema.string[ApiStatus]

  given Schema[StatusCode] = Schema(SInteger())
}

case class ApiError(code: String,
                    message: String)

case class ApiErrorResponse(@encodedExample("error") status: ApiStatus,
                            statusCode: StatusCode,
                            error: ApiError) extends ApiResponse

object ApiErrorResponse {
  implicit val apiErrorResponseEncoder: Encoder[ApiErrorResponse] = Encoder
    .forProduct3("status", "statusCode", "error")((apiErrorResponse: ApiErrorResponse) =>
      (apiErrorResponse.status.toString.toLowerCase, apiErrorResponse.statusCode.code, apiErrorResponse.error)
    )

  def apply(statusCode: StatusCode, error: ApiError): ApiErrorResponse = {
    new ApiErrorResponse(ApiStatus.Error, statusCode, error)
  }

  def apply(statusCode: StatusCode, throwable: Throwable): ApiErrorResponse = {
    ApiErrorResponse(statusCode, ApiError(
      code = throwable.getClass.getSimpleName,
      message = throwable.getMessage
    ))
  }

  def rejection(statusCode: StatusCode, message: String): ApiErrorResponse = {
    ApiErrorResponse(statusCode, ApiError("Rejection", message))
  }

  def decodeFailure(statusCode: StatusCode, message: String): ApiErrorResponse = {
    ApiErrorResponse(statusCode, ApiError("DecodeFailure", message))
  }
}

case class ApiSuccessResponse(@encodedExample("success") status: ApiStatus,
                              statusCode: StatusCode)
  extends ApiResponse

object ApiSuccessResponse {
  implicit val apiSuccessResponseEncoder: Encoder[ApiSuccessResponse] = Encoder
    .forProduct2("status", "statusCode")((apiSuccessResponse: ApiSuccessResponse) =>
      (apiSuccessResponse.status.toString.toLowerCase, apiSuccessResponse.statusCode.code)
    )

  def apply() = {
    new ApiSuccessResponse(ApiStatus.Success, StatusCode.Ok)
  }
}

case class ApiSuccessResponseWithData[T](@encodedExample("success") status: ApiStatus,
                                         statusCode: StatusCode,
                                         data: T)
  extends ApiResponse

object ApiSuccessResponseWithData {
  implicit def apiSuccessResponseWithDataEncoder[T](implicit enc: Encoder[T]): Encoder[ApiSuccessResponseWithData[T]] = Encoder
    .forProduct3("status", "statusCode", "data")((apiSuccessResponseWithData: ApiSuccessResponseWithData[T]) =>
      (apiSuccessResponseWithData.status.toString.toLowerCase, apiSuccessResponseWithData.statusCode.code, apiSuccessResponseWithData.data)
    )

  def apply[T](data: T) = {
    new ApiSuccessResponseWithData(ApiStatus.Success, StatusCode.Ok, data)
  }
}
