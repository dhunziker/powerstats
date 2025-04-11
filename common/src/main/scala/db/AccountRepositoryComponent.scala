package ai.powerstats.common
package db

import db.model.{Account, AccountStatus}

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDateTime

trait AccountRepositoryComponent {
  val accountRepository: AccountRepository

  trait AccountRepository {
    def findAccount(email: String, xa: Transactor[IO]): IO[Option[Account]] = {
      sql"""select id, email, password_hash, status, creation_date 
            from account
            where email = $email""".stripMargin
        .query[Account]
        .option
        .transact(xa)
    }

    def createAccount(email: String, passwordHash: Array[Byte], xa: Transactor[IO]): IO[Int] = {
      sql"""insert into account (
              email,
              password_hash,
              status,
              creation_date
            ) values (
              $email,
              $passwordHash,
              ${AccountStatus.Provisional},
              ${LocalDateTime.now()}
            )""".stripMargin
        .update
        .run
        .transact(xa)
    }
  }
}
