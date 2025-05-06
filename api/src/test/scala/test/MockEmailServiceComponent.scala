package dev.powerstats.api
package test

import service.EmailServiceComponent

import cats.effect.IO
import cats.effect.kernel.Ref
import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.email.{RespMessage, RespMessages, RespTo}
import org.http4s.client.Client

trait MockEmailServiceComponent extends EmailServiceComponent with ConfigComponent {
  val emailService: EmailService

  trait MockEmailService extends EmailService {
    private val request: Ref[IO, Boolean] = Ref.unsafe(false)
    private val response: Ref[IO, RespMessages] = Ref.unsafe(RespMessages(List(RespMessage("success", List(RespTo("", "", 1L, ""))))))

    def requested(): IO[Boolean] = {
      request.get
    }

    def updateResponse(status: String): IO[Unit] = {
      response.update(resp => RespMessages(List(resp.messages.head.copy(status = status))))
    }

    override def sendActivationEmail(client: Client[IO], templateId: Long, toAddress: String, toName: String, activationLink: String) = {
      for {
        _ <- request.set(true)
        resp <- response.get
      } yield resp
    }
  }
}
