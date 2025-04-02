package ai.powerstats.common
package db

import model.ApiKey

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

trait ApiKeyRepositoryComponent {
  val apiKeyRepository: ApiKeyRepository

  trait ApiKeyRepository {
    def selectApiKey(accountId: Long, xa: Transactor[IO]): IO[List[ApiKey]] = {
      sql"""select id, account_id, key, creation_date, expiry_date
            from api_key
            where account_id = $accountId""".stripMargin
        .query[ApiKey]
        .to[List]
        .transact(xa)
    }

    def insertApiKey(apiKey: ApiKey, xa: Transactor[IO]): IO[Int] = {
      sql"""insert into api_key (
              account_id,
              key,
              creation_date,
              expiry_date
            ) values (
              ${apiKey.accountId},
              ${apiKey.key},
              ${apiKey.creationDate},
              ${apiKey.expiryDate}
            )""".stripMargin
        .update
        .run
        .transact(xa)
    }
  }
}
