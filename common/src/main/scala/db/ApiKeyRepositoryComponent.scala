package dev.powerstats.common
package db

import db.model.ApiKey

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.LocalDateTime

trait ApiKeyRepositoryComponent {
  val apiKeyRepository: ApiKeyRepository

  trait ApiKeyRepository {
    def countApiKeys(accountId: Long, xa: Transactor[IO]): IO[Int] = {
      sql"""select count(1)
            from api_key
            where account_id = $accountId"""
        .query[Int]
        .unique
        .transact(xa)
    }

    def findApiKeys(accountId: Long, xa: Transactor[IO]): IO[List[ApiKey]] = {
      findApiKeys(accountId = Some(accountId))
        .query[ApiKey]
        .to[List]
        .transact(xa)
    }

    def findApiKeys(publicKey: String, xa: Transactor[IO]): IO[ApiKey] = {
      findApiKeys(publicKey = Some(publicKey))
        .query[ApiKey]
        .unique
        .transact(xa)
    }

    def insertApiKey(accountId: Long,
                     name: String,
                     publicKey: String,
                     secretKeyHash: Array[Byte],
                     creationDate: LocalDateTime,
                     expiryDate: LocalDateTime,
                     xa: Transactor[IO]): IO[ApiKey] = {
      val insertQuery = Update[(Long, String, String, Array[Byte], LocalDateTime, LocalDateTime)](
        """
          insert into api_key (
              account_id,
              name,
              public_key,
              secret_key_hash,
              creation_date,
              expiry_date
            ) values (?, ?, ?, ?, ?, ?)
        """
      )
      val insertAndSelect = for {
        id <- insertQuery.withUniqueGeneratedKeys[Long]("id")(
          (accountId, name, publicKey, secretKeyHash, creationDate, expiryDate)
        )
        apiKey <- findApiKeys(id = Some(id)).query[ApiKey].unique
      } yield apiKey
      insertAndSelect.transact(xa)
    }

    def deleteApiKey(id: Long, accountId: Long, xa: Transactor[IO]): IO[Int] = {
      val deleteQuery = Update[(Long, Long)](
        """
          delete from api_key
          where id = ?
          and account_id = ?
        """
      )
      deleteQuery
        .run((id, accountId))
        .transact(xa)
    }

    private def findApiKeys(id: Option[Long] = None, accountId: Option[Long] = None, publicKey: Option[String] = None): Fragment = {
      val baseQuery =
        fr"""
          select
            id,
            account_id,
            name,
            public_key,
            secret_key_hash,
            creation_date,
            expiry_date
          from api_key
        """
      val idFilter = id.map(v => fr"id = $v")
      val accountIdFilter = accountId.map(v => fr"account_id = $v")
      val publicKeyFilter = publicKey.map(v => fr"public_key = $v")
      baseQuery ++ Fragments.whereAndOpt(idFilter, accountIdFilter, publicKeyFilter)
    }
  }
}
