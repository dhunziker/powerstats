package ai.powerstats.common
package db

import model.Account

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

trait AccountRepositoryComponent {
  val accountRepository: AccountRepository

  trait AccountRepository {
    def selectAccount(email: String, xa: Transactor[IO]): IO[Option[Account]] = {
      sql"""select id, email, password_hash 
            from account
            where email = $email""".stripMargin
        .query[Account]
        .option
        .transact(xa)
    }

    def insertAccount(email: String, passwordHash: Array[Byte], xa: Transactor[IO]): IO[Int] = {
      sql"""insert into account (
              email,
              password_hash
            ) values (
              $email,
              $passwordHash
            )""".stripMargin
        .update
        .run
        .transact(xa)
    }

    def updateAccount(email: String, passwordHash: Array[Byte], xa: Transactor[IO]): IO[Int] = {
      sql"""update account
            set password_hash = $passwordHash
            where email = $email""".stripMargin
        .update
        .run
        .transact(xa)
    }
  }
}
