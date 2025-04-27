package ai.powerstats.api
package test

import service.EmailServiceComponent

import ai.powerstats.common.db.model.Account
import ai.powerstats.common.email.{ReqMessages, RespMessage, RespMessages, RespTo}
import cats.effect.kernel.Ref
import cats.effect.{Deferred, IO}
import org.http4s.client.Client

import java.util.UUID

trait MockEmailServiceComponent extends EmailServiceComponent {

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
