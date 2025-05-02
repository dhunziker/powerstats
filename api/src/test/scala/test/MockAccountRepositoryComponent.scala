package dev.powerstats.api
package test

import dev.powerstats.common.db.AccountRepositoryComponent
import dev.powerstats.common.db.model.{Account, AccountStatus}
import cats.effect.kernel.Ref
import cats.effect.{IO, Ref}
import doobie.Transactor

import java.time.LocalDateTime

trait MockAccountRepositoryComponent extends AccountRepositoryComponent {
  val accountRepository: AccountRepository

  trait MockAccountRepository extends AccountRepository {
    private val storage: Ref[IO, Map[Long, Account]] = Ref.unsafe(Map.empty)
    private val iterator = Iterator.from(0)

    override def findAccount(email: String, xa: Transactor[IO]): IO[Option[Account]] = {
      for {
        prev <- storage.get
        account = prev.values.find(a => a.email == email)
      } yield account
    }

    override def insertAccount(email: String, passwordHash: Array[Byte], xa: Transactor[IO]): IO[Account] = {
      for {
        account <- IO.pure(Account(
          iterator.next().toLong,
          email,
          passwordHash,
          AccountStatus.Provisional,
          LocalDateTime.now()))
        _ <- storage.update(_.updated(account.id, account))
      } yield account
    }

    override def updateAccount(id: Long, xa: Transactor[IO], passwordHash: Option[Array[Byte]], status: Option[AccountStatus]) = {
      for {
        prev <- storage.get
        account = prev(id)
        updated = account.copy(
          passwordHash = passwordHash.getOrElse(account.passwordHash),
          status = status.getOrElse(account.status)
        )
        _ <- storage.update(_.updated(updated.id, updated))
      } yield updated
    }
  }
}
