import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession

object Application extends App {
/*
  import datamodel.latest.schema.tables._
  import datamodel.latest.schema.Version
  println("Users in the database:")
  val dblocation = System.getProperty("user.dir") + "/test.tb"
  println(Database.forURL(
    s"jdbc:h2:$dblocation", driver = "org.h2.Driver").withDynSession {
    Users.map(u=>u).list
  })
 */
  println("The body of App.scala is currently commented out (activate after at least version 1)")
}
