package dev.powerstats.common
package db

import config.{ConfigComponent, Database}

import cats.effect.{IO, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

trait DatabaseTransactorComponent {
  this: ConfigComponent =>
  val databaseTransactor: DatabaseTransactor

  trait DatabaseTransactor {
    private val PostgresDriver = "org.postgresql.Driver"

    def init(database: IO[Database]): Resource[IO, Transactor[IO]] = {
      for {
        dbConfig <- database.toResource
        ec <- ExecutionContexts.fixedThreadPool[IO](32)
        hikariConfig <- Resource.pure[IO, HikariConfig] {
          val config = new HikariConfig()
          config.setDriverClassName(PostgresDriver)
          config.setJdbcUrl(dbConfig.url)
          config.setUsername(dbConfig.user)
          config.setPassword(dbConfig.password)
          config.setMaximumPoolSize(dbConfig.maxPoolSize)
          config
        }
        xa <- HikariTransactor.fromHikariConfigCustomEc[IO](hikariConfig, ec)
      } yield xa
    }
  }
}
