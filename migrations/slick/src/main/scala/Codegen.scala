package com.liyaos.forklift.slick

import java.io._
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import slick.model.Model
import slick.jdbc.JdbcProfile
import slick.codegen.SourceCodeGenerator

trait SlickCodegen {

  val generatedDir =
    System.getProperty("user.dir") + "/generated_code/src/main/scala"

  val container = "tables"

  val fileName = "schema.scala"

  def pkgName(version: String) = "datamodel." + version + ".schema"

  def tableNames: Seq[String] = List()

  def getTables(driver: JdbcProfile) = driver.createModel(Some(
    driver.defaultTables.map { s =>
      s.filter { t =>
        tableNames.contains(t.name.name)
      }
    }))

  class SlickSourceCodeGenerator(m: Model, version: Int)
      extends SourceCodeGenerator(m) {
    override def packageCode(
      profile: String, pkg: String,
      container: String, parentType: Option[String]) : String =
      super.packageCode(profile, pkg, container, None) + s"""
object Version{
  def version = $version
}
"""
  }

  def getGenerator(m: Model, version: Int) =
    new SlickSourceCodeGenerator(m, version)

  val waitDuration = Duration.Inf

  def genCode(mm: SlickMigrationManager) {
    import mm.dbConfig.profile.api._
    if (mm.notYetAppliedMigrations.size > 0) {
      println("Your database is not up to date, code generation denied for compatibility reasons. Please update first.")
      return
    }
    val driver = mm.dbConfig.profile
    val action = getTables(driver).flatMap { case m =>
        DBIO.from {
          Future {
            val latest = mm.latest
            latest match {
              case Some(latestVersion) =>
                List( "v" + latestVersion, "latest" ).foreach { version =>
                  val generator = getGenerator(m, latestVersion)
                  generator.writeToFile(s"${driver.toString.dropRight(1)}",
                    generatedDir, pkgName(version), container, fileName)
                }
              case None =>
                println("No migrations are applied yet, so nothing to be generated.")
            }
          }
        }
    }
    val f = mm.db.run(action)
    Await.result(f, waitDuration)
  }

  def remove() {
    try {
      val f = (Glob.glob((f: File) => !f.isDirectory && f.getName == fileName)
        (List(generatedDir)))
      f.foreach(_.delete)
    } catch {
      case e: FileNotFoundException =>
    }
  }
}

object Glob{
  // taken from: http://kotakanbe.blogspot.ch/2010/11/scaladirglobsql.html
  def glob(filter: (File) => Boolean)(dirs: List[String]): List[File] = {
    def recursive(dir: File, acc: List[File]): List[File] =
      Option(dir.listFiles) match {
        case None => throw new FileNotFoundException(dir.getAbsolutePath)
        case Some(lists) =>
          val filtered = lists.filter{ c =>  filter(c) }.toList
          val childDirs = lists.filter{ c => c.isDirectory && !c.getName.startsWith(".") }
          return childDirs.foldLeft(acc ::: filtered){ (a, dir) => recursive(dir, a)}
      }
    dirs.flatMap{ d => recursive(new File(d), Nil)}
  }
}
