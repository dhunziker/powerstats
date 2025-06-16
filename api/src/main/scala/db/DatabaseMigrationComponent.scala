package dev.powerstats.api
package db

import cats.effect.IO
import dev.powerstats.common.config.Database
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

trait DatabaseMigrationComponent {
  val databaseMigration: DatabaseMigration

  trait DatabaseMigration {
    def migrate(database: IO[Database]): IO[MigrateResult] = for {
      dbConfig <- database
      result = Flyway
        .configure()
        .baselineOnMigrate(true)
        .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
        .load()
        .migrate()
    } yield result
  }
}
