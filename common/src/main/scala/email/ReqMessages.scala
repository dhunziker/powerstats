package ai.powerstats.common
package email

import io.circe.Encoder

case class ReqMessages(messages: List[ReqMessage])

object ReqMessages {
  implicit val encodeMessages: Encoder[ReqMessages] = Encoder.forProduct1("Messages")(_.messages)
}

case class ReqMessage(from: ReqFrom,
                      to: List[ReqTo],
                      templateId: Long,
                      templateLanguage: Boolean,
                      subject: String,
                      variables: Map[String, String])

object ReqMessage {
  implicit val encodeMessage: Encoder[ReqMessage] = Encoder.forProduct6(
    "From",
    "To",
    "TemplateID",
    "TemplateLanguage",
    "Subject",
    "Variables"
  )(m => (m.from, m.to, m.templateId, m.templateLanguage, m.subject, m.variables))
}

case class ReqFrom(email: String, name: String)

object ReqFrom {
  implicit val encodeFrom: Encoder[ReqFrom] = Encoder.forProduct2("Email", "Name")(f => (f.email, f.name))
}

case class ReqTo(email: String, name: String)

object ReqTo {
  implicit val encodeTo: Encoder[ReqTo] = Encoder.forProduct2("Email", "Name")(t => (t.email, t.name))
}
