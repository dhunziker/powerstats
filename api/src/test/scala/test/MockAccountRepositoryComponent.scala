package dev.powerstats.api
package test

import cats.effect.IO
import dev.powerstats.common.db.AccountRepositoryComponent
import dev.powerstats.common.db.model.{Account, AccountStatus}
import doobie.Transactor

import java.time.LocalDateTime

trait MockAccountRepositoryComponent extends AccountRepositoryComponent {
  val accountRepository: AccountRepository

  trait MockAccountRepository extends AccountRepository with MockRepository[Account] {

    override def findAccount(email: String, xa: Transactor[IO]): IO[Option[Account]] = {
      findInStorage(account => account.email == email)
    }

    override def insertAccount(email: String, passwordHash: Array[Byte], xa: Transactor[IO]): IO[Account] = {
      for {
        account <- IO.pure(Account(
          nextId,
          email,
          passwordHash,
          AccountStatus.Provisional,
          LocalDateTime.now()))
        _ <- addToStorage(account.id, account)
      } yield account
    }

    override def updateAccount(id: Long, xa: Transactor[IO], passwordHash: Option[Array[Byte]], status: Option[AccountStatus]) = {
      for {
        prev <- getStorage
        account = prev(id)
        updated = account.copy(
          passwordHash = passwordHash.getOrElse(account.passwordHash),
          status = status.getOrElse(account.status)
        )
        _ <- addToStorage(updated.id, updated)
      } yield updated
    }
  }
}
