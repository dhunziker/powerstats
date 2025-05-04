package dev.powerstats.api
package service

import cats.effect.*
import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.email.*
import dev.powerstats.common.email.RespMessages.*
import dev.powerstats.common.logging.LoggingComponent
import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*
import org.typelevel.log4cats.LoggerFactory

trait EmailServiceComponent
  extends ConfigComponent
    with LoggingComponent {
  val emailService: EmailService

  trait EmailService {
    private val logger = LoggerFactory[IO].getLogger

    def sendActivationEmail(client: Client[IO], templateId: Long, toAddress: String, toName: String, activationLink: String): IO[RespMessages] = for {
      apiConfig <- config.apiConfig
      mailjetConfig <- config.mailjetConfig
      from = ReqFrom(mailjetConfig.fromAddress, mailjetConfig.fromName)
      to = ReqTo(toAddress, toName)
      messages = ReqMessages(List(
        ReqMessage(
          from,
          List(to),
          templateId,
          templateLanguage = true,
          "PowerStats - Activate your account",
          Map("activation_link" -> activationLink)))
      )
      request = POST(messages.asJson, Uri.unsafeFromString(s"${mailjetConfig.baseUrl}/send"))
      response <- client.expect[RespMessages](request)
    } yield response
  }
}
