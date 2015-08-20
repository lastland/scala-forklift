import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.H2Driver.api._

object Application {
  def main(args: Array[String]) {
    /*
    try {
      import datamodel.latest.schema.tables._
      import datamodel.latest.schema.Version
      val f = MyDatabase.db.run {
        Users.result
      } map { users =>
        println("Users in the database:")
        for (user <- users) println(user)
      }
      Await.result(f, Duration.Inf)
    } finally {
      MyDatabase.db.close()
    }
    return
     */
    println("The body of App.scala is currently commented out (activate after at least version 1)")
  }
}
