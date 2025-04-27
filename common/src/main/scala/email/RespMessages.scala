package ai.powerstats.common
package email

import io.circe.Decoder

case class RespMessages(messages: List[RespMessage])

object RespMessages {
  implicit val decodeMessages: Decoder[RespMessages] = Decoder.forProduct1("Messages")(RespMessages.apply)
}

case class RespMessage(status: String,
                       to: List[RespTo])

object RespMessage {
  implicit val decodeMessage: Decoder[RespMessage] = Decoder.forProduct2("Status", "To")(RespMessage.apply)
}

case class RespTo(email: String, messageUuid: String, messageId: Long, messageHref: String)

object RespTo {
  implicit val decodeTo: Decoder[RespTo] = Decoder.forProduct4(
    "Email",
    "MessageUUID",
    "MessageID",
    "MessageHref"
  )(RespTo.apply)
}
