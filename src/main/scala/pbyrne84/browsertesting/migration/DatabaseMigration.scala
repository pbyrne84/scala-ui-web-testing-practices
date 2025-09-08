package pbyrne84.browsertesting.migration

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import pbyrne84.browsertesting.models.DbConfig

class DatabaseMigration(dbConfig: DbConfig) {

  // This url would not be compatible with real Postgres db. MODE switches H2

  def migrate: MigrateResult = {
    val flyway = Flyway.configure.dataSource(dbConfig.url, dbConfig.user, dbConfig.password).load

    flyway.migrate()
  }
}
