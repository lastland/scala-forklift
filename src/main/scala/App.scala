import scala.slick.migrations._
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession
object App{
  def run(mm:MyMigrationManager){
/*
    import datamodel.latest.schema.tables._
    import datamodel.latest.schema.Version
    if( Version.version != mm.latest ){
      println("!!Generated code is outdated, please run code generator") // or you could also do it automatically here
      return
    }
    println("Users in the database:")
    println(
      mm.db.withDynSession{
        Users.map(u=>u).list
      }
    )
    return
*/
    println("The body of App.scala is currently commented out (activate after at least version 1)")
  }
}
