package dev.powerstats.api
package route.request

import io.circe.generic.auto.*
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.Schema

enum ApiStatus {
  case Success, Error
}

sealed trait ApiResponse {
  def status: ApiStatus

  def statusCode: StatusCode
}

case class ApiError(code: String,
                    message: String)

case class ApiErrorResponse(statusCode: StatusCode,
                            error: ApiError) extends ApiResponse {
  override val status: ApiStatus = ApiStatus.Error
}

object ApiErrorResponse {
  given Decoder[StatusCode] = Decoder.decodeInt.map(StatusCode.apply)

  given Schema[StatusCode] = Schema.string[StatusCode]

  implicit val apiErrorResponseEncoder: Encoder[ApiErrorResponse] = Encoder
    .forProduct3("status", "statusCode", "error")((apiErrorResponse: ApiErrorResponse) =>
      (apiErrorResponse.status.toString.toLowerCase, apiErrorResponse.statusCode.code, apiErrorResponse.error)
    )

  def apply(statusCode: StatusCode, message: String): ApiErrorResponse = {
    ApiErrorResponse(statusCode, ApiError("DecodeFailure", message))
  }
}

case class ApiSuccessResponse() extends ApiResponse {
  override val status: ApiStatus = ApiStatus.Success
  override val statusCode = StatusCode.Ok
}

object ApiSuccessResponse {
  implicit val apiSuccessResponseEncoder: Encoder[ApiSuccessResponse] = Encoder
    .forProduct2("status", "statusCode")((apiSuccessResponse: ApiSuccessResponse) =>
      (apiSuccessResponse.status.toString.toLowerCase, apiSuccessResponse.statusCode.code)
    )
}

case class ApiSuccessResponseWithData[T](data: T) extends ApiResponse {
  override val status: ApiStatus = ApiStatus.Success
  override val statusCode = StatusCode.Ok
}

object ApiSuccessResponseWithData {
  implicit def apiSuccessResponseWithDataEncoder[T](implicit enc: Encoder[T]): Encoder[ApiSuccessResponseWithData[T]] = Encoder
    .forProduct3("status", "statusCode", "data")((apiSuccessResponseWithData: ApiSuccessResponseWithData[T]) =>
      (apiSuccessResponseWithData.status.toString.toLowerCase, apiSuccessResponseWithData.statusCode.code, apiSuccessResponseWithData.data)
    )
}
