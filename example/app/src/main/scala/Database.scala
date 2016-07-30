import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object MyDatabase {
  lazy val config = DatabaseConfig.forConfig[JdbcProfile]("slick")
  lazy val db = config.db
}
