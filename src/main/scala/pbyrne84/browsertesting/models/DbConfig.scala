package pbyrne84.browsertesting.models

import com.typesafe.config.Config

object DbConfig {

  def fromConfig(config: Config): DbConfig = {
    val dbConfig = config.getObject("db").toConfig
    val dbName   = dbConfig.getString("dbName")
    val user     = dbConfig.getString("user")
    val password = dbConfig.getString("password")
    val port     = dbConfig.getInt("port")

    DbConfig(dbName, user, password, port)
  }
}

case class DbConfig(dbName: String, user: String, password: String, port: Int) {
  // CREATE DATABASE practices_db;
  val url: String =
    s"jdbc:postgresql://localhost/$dbName?user=$user&password=$password&createDatabaseIfNotExist=true"
}
