package ai.powerstats.api
package service

import ai.powerstats.common.config.ConfigComponent
import ai.powerstats.common.email.RespMessages.*
import ai.powerstats.common.email.*
import ai.powerstats.common.logging.LoggingComponent
import cats.effect.*
import io.circe.Decoder
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.LoggerFactory
import io.circe.*
import io.circe.generic.auto.*

trait EmailServiceComponent
  extends ConfigComponent
    with LoggingComponent {
  val emailService: EmailService

  trait EmailService {
    private val logger = LoggerFactory[IO].getLogger

    def sendActivationEmail(client: Client[IO], templateId: Long, toAddress: String, toName: String, activationLink: String): IO[RespMessages] = for {
      apiConfig <- config.appConfig.map(_.api)
      mailjetConfig <- config.appConfig.map(_.mailjet)
      from = ReqFrom(mailjetConfig.fromAddress, mailjetConfig.fromName)
      to = ReqTo(toAddress, toName)
      messages = ReqMessages(List(
        ReqMessage(
          from,
          List(to),
          templateId,
          templateLanguage = false,
          "PowerStats - Activate your account",
          Map("activationLink" -> activationLink)))
      )
      request = POST(messages.asJson, Uri.unsafeFromString(s"${mailjetConfig.baseUrl}/send"))
      response <- client.expect[RespMessages](request)
    } yield response
  }
}
