package ai.powerstats.common
package db

import config.{Database, SettingsComponent}

import cats.effect.{IO, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor

trait DbTransactorComponent {
  this: SettingsComponent =>
  val transactor: DbTransactor

  trait DbTransactor {
    private val PostgresDriver = "org.postgresql.Driver"

    def init(database: IO[Database]): Resource[IO, Transactor[IO]] = {
      for {
        dbConfig <- database.toResource
        hikariConfig <- Resource.pure {
          val config = new HikariConfig()
          config.setDriverClassName(PostgresDriver)
          config.setJdbcUrl(dbConfig.url)
          config.setUsername(dbConfig.user)
          config.setPassword(dbConfig.password)
          config.setMaximumPoolSize(dbConfig.maxPoolSize)
          config
        }
        xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
      } yield xa
    }
  }
}
