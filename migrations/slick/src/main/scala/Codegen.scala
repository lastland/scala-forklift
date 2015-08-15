package scala.migrations.slick

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcBackend
import slick.driver.JdbcDriver
import slick.driver.JdbcDriver.simple._
import slick.codegen.SourceCodeGenerator
import Database.dynamicSession

trait SlickCodegen {

  val generatedDir =
    System.getProperty("user.dir") + "/generated_code/src/main/scala"

  val container = "tables"

  val fileName = "schema.scala"

  def pkgName(version: String) = "datamodel." + version + ".schema"

  def tableNames: Seq[String] = List()

  def genCode(mm: SlickMigrationManager) {
    import mm.config.driver.api._
    if (mm.notYetAppliedMigrations.size > 0) {
      println("Your database is not up to date, code generation denied for compatibility reasons. Please update first.")
      return
    }
    val driver = mm.config.driver
    val model = driver.createModel(Some(
      driver.defaultTables.map { s =>
        s.filter { t =>
          tableNames.contains(t.name.name)
        }
      }))
    val f = mm.db.run(model)
    f onSuccess { case m =>
      val latest = mm.latest
      List( "v" + latest, "latest" ).foreach { version =>
        val generator = new SourceCodeGenerator(m) {
          override def packageCode(
            profile: String, pkg: String,
            container: String, parentType: Option[String]) : String =
            super.packageCode(profile, pkg, container, None) + s"""
object Version{
  def version = $latest
}
"""
        }
        generator.writeToFile(s"slick.driver.${driver}",
          generatedDir, pkgName(version), container, fileName)
      }
    }
    Await.result(f, Duration.Inf)
  }
}
