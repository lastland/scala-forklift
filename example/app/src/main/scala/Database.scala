import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

object MyDatabase {
  lazy val config = DatabaseConfig.forConfig[JdbcProfile]("slick")
  lazy val db = config.db
}
