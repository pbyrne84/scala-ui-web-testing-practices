package pbyrne84.browsertesting.repo

import com.typesafe.config.ConfigFactory
import pbyrne84.browsertesting.models.DbConfig
import slick.jdbc.PostgresProfile

object SearchDatabase {

  lazy val config = ConfigFactory.load()
  lazy val dbConfig: DbConfig = DbConfig.fromConfig(config)

  import slick.jdbc.PostgresProfile.api._
  val db: PostgresProfile.backend.JdbcDatabaseDef = Database.forURL(dbConfig.url)

}
