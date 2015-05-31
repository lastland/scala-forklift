import com.typesafe.config._
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession

object MyDatabase {
  private val config = ConfigFactory.load()
  val dburl = config.getString("migrations.db.url")
  val dbdriver = config.getString("migrations.db.driver")
  def db = Database.forURL(dburl, driver = dbdriver)
}
