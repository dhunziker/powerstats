package dev.powerstats.api
package test

import service.{AccountServiceComponent, ClockComponent, HashingServiceComponent}

import dev.powerstats.common.config.ConfigComponent
import dev.powerstats.common.db.AccountRepositoryComponent
import dev.powerstats.common.logging.LoggingComponent
import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.time.Clock

trait MockAccountServiceComponent extends AccountServiceComponent {
  this: ConfigComponent &
    LoggingComponent &
    ClockComponent &
    HashingServiceComponent &
    MockEmailServiceComponent &
    MockAccountRepositoryComponent =>
  override implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  override val config = new Config {}
  override val hashingService: HashingService = new HashingService {}
  override val emailService: EmailService = new MockEmailService {}
  override val accountRepository: AccountRepository = new MockAccountRepository {}
  override val accountService = new AccountService {}
}
