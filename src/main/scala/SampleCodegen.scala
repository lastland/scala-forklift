import scala.slick.jdbc.codegen
import scala.slick.jdbc.reflect
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import scala.slick.migrations._
import Database.threadLocalSession
object SampleCodegen{
  def gen(mm:MyMigrationManager){
    mm.db withSession {
      if(mm.notYetAppliedMigrations.size > 0){
        println("Your database is not up to date, code generation denied for compatibility reasons. Please update first.")
        return
      }
      class MyTableGen (schema:codegen.Schema,table:reflect.Table) extends codegen.Table(schema, table){
        override def entityName = Map(
          "users" -> "User"
        )(name)
      }
      
      val latest = mm.latest
      List( "v" + latest, "latest" ).foreach{
        version =>
          val pkg = "datamodel." + version + ".schema"
          val generator = new codegen.Schema(
            "H2",
            new scala.slick.jdbc.reflect.Schema((List("users"))),
            pkg
          ){
            override def table( t:reflect.Table ) = new MyTableGen(this,t)
            override def render = super.render + s"""
package $pkg.version{
  object Version{
    def version = $latest
  }
}
"""
          }
          val folder = System.getProperty("user.dir")+"/src/main/scala"
          generator.singleFile(folder)
      }
    }
  }
}