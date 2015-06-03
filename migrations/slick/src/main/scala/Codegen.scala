package scala.migrations.slick

import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import scala.slick.codegen.SourceCodeGenerator
import Database.dynamicSession

trait SlickCodegen {

  val generatedDir =
    System.getProperty("user.dir") + "/generated_code/src/main/scala"

  val container = "tables"

  val fileName = "schema.scala"

  def pkgName(version: String) = "datamodel." + version + ".schema"

  def tableNames: Seq[String] = List()

  def genCode(mm: SlickMigrationManager){
    mm.db withDynSession {
      if(mm.notYetAppliedMigrations.size > 0){
        println("Your database is not up to date, code generation denied for compatibility reasons. Please update first.")
        return
      }
      val model = H2Driver.createModel(Some(
        H2Driver.defaultTables.filter(t =>
          tableNames.contains(t.name.name))))
      val latest = mm.latest
      List( "v" + latest, "latest" ).foreach { version =>
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
          generatedDir, pkgName(version), container, fileName)
      }
    }
  }
}
