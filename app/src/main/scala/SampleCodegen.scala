import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import scala.slick.codegen.SourceCodeGenerator
import scala.migrations.slick._

import Database.dynamicSession
object SampleCodegen{
  def gen(mm:MyMigrationManager){
    mm.db withDynSession {
      if(mm.notYetAppliedMigrations.size > 0){
        println("Your database is not up to date, code generation denied for compatibility reasons. Please update first.")
        return
      }
      val tableNames = List("users")
      val model = H2Driver.createModel(Some(
        H2Driver.defaultTables.filter(t =>
          tableNames.contains(t.name.name))))
      val latest = mm.latest
      List( "v" + latest, "latest" ).foreach{ version =>
        val pkg = "datamodel." + version + ".schema"
        val folder = System.getProperty("user.dir")+"/app/src/main/scala"
        val generator = new SourceCodeGenerator(model) {
          override def packageCode(
            profile: String, pkg: String, container: String) : String =
            super.packageCode(profile, pkg, container) + s"""
object Version{
  def version = $latest
}
"""
        }
        generator.writeToFile("scala.slick.driver.H2Driver",
          folder, pkg, "tables", "schema.scala")
      }
    }
  }
}
