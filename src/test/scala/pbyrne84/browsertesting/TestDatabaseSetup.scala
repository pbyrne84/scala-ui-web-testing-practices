package pbyrne84.browsertesting

import com.typesafe.config.ConfigFactory
import pbyrne84.browsertesting.models.DbConfig
import pbyrne84.browsertesting.setup.DatabaseSetup
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext

trait TestDatabaseSetup {

  protected implicit val ec: ExecutionContext

  private lazy val config               = ConfigFactory.load()
  protected lazy val dbConfig: DbConfig = DbConfig.fromConfig(config)

  import slick.jdbc.PostgresProfile.api.*
  protected lazy val db: PostgresProfile.backend.JdbcDatabaseDef = Database.forURL(
    dbConfig.url
  )

  protected lazy val databaseSetup: DatabaseSetup = new DatabaseSetup(dbConfig, db)
}
