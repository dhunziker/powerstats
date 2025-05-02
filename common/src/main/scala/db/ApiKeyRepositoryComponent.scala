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
    def findApiKeys(accountId: Long, xa: Transactor[IO]): IO[List[ApiKey]] = {
      sql"""select
              id,
              account_id,
              name,
              key_hash,
              creation_date,
              expiry_date
            from api_key
            where account_id = $accountId
            order by creation_date""".stripMargin
        .query[ApiKey]
        .to[List]
        .transact(xa)
    }

    def insertApiKey(accountId: Long,
                     name: String,
                     keyHash: Array[Byte],
                     creationDate: LocalDateTime,
                     expiryDate: LocalDateTime,
                     xa: Transactor[IO]): IO[ApiKey] = {
      (for {
        id <- sql"""insert into api_key (
              account_id,
              name,
              key_hash,
              creation_date,
              expiry_date
            ) values (
              $accountId,
              $name,
              $keyHash,
              $creationDate,
              $expiryDate
            )"""
          .stripMargin
          .update
          .withUniqueGeneratedKeys[Long]("id")
        apiKey <- sql"""select
              id,
              account_id,
              name,
              key_hash,
              creation_date,
              expiry_date
            from api_key
            where id = $id"""
          .stripMargin
          .query[ApiKey]
          .unique
      } yield apiKey).transact(xa)
    }

    def deleteApiKey(id: Long, accountId: Long, xa: Transactor[IO]): IO[Int] = {
      sql"""delete from api_key
            where id = $id
            and account_id = $accountId""".stripMargin
        .update
        .run
        .transact(xa)
    }
  }
}
