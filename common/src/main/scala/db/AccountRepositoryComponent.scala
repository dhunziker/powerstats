package dev.powerstats.common
package db

import db.model.{Account, AccountStatus}

import cats.data.NonEmptyList
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDateTime

trait AccountRepositoryComponent {
  val accountRepository: AccountRepository

  trait AccountRepository {
    def findAccount(email: String, xa: Transactor[IO]): IO[Option[Account]] = {
      findAccount(email = Some(email))
        .query[Account]
        .option
        .transact(xa)
    }

    def insertAccount(email: String, passwordHash: Array[Byte], xa: Transactor[IO]): IO[Account] = {
      val insertQuery = Update[(String, Array[Byte], AccountStatus, LocalDateTime)](
        """
          insert into account (
            email,
            password_hash,
            status,
            creation_date
          ) values (?, ?, ?, ?)
        """
      )
      val insertAndSelect = for {
        id <- insertQuery.withUniqueGeneratedKeys[Long]("id")(
          (email, passwordHash, AccountStatus.Provisional, LocalDateTime.now())
        )
        account <- findAccount(id = Some(id)).query[Account].unique
      } yield account
      insertAndSelect.transact(xa)
    }

    def updateAccount(id: Long, xa: Transactor[IO], passwordHash: Option[Array[Byte]] = None, status: Option[AccountStatus] = None): IO[Account] = {
      val setFragments = List(
        passwordHash.map(ph => fr"password_hash = $ph"),
        status.map(s => fr"status = $s")
      ).flatMap(_.toList)
      val updateQuery = fr"update account" ++
        Fragments.set(NonEmptyList.fromListUnsafe(setFragments)) ++
        fr"where id = $id"
      val updateAndSelect = for {
        _ <- updateQuery.update.run
        account <- findAccount(id = Some(id)).query[Account].unique
      } yield account
      updateAndSelect.transact(xa)
    }

    private def findAccount(id: Option[Long] = None, email: Option[String] = None, status: Option[AccountStatus] = None): Fragment = {
      val baseQuery =
        fr"""
          select
            id,
            email,
            password_hash,
            status,
            creation_date
          from account
        """
      val idFilter = id.map(i => fr"id = $i")
      val emailFilter = email.map(e => fr"email = $e")
      val statusFilter = status.map(s => fr"status = $s")
      baseQuery ++ Fragments.whereAndOpt(idFilter, emailFilter, statusFilter)
    }
  }
}
