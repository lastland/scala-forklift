package com.liyaos.forklift.slick.tests.subprojects

import org.scalatest._
import java.io.File
import scala.language.implicitConversions
import org.apache.commons.io.FileUtils
import ammonite.ops._

class CommandsTest extends FlatSpec
    with BeforeAndAfter {
  val wd = pwd
  val dir = TestDir.createTestDir(wd)
  val testDir = dir.testPath

  implicit def pathToString(path: Path): String = path.toString

  val unhandled = testDir/Symbol("migrations")/Symbol("src_migrations")/Symbol("main")/Symbol("scala")
  val handled = testDir/Symbol("migrations")/Symbol("src")/Symbol("main")/Symbol("scala")/Symbol("migrations")

  before {
    dir.setup()
  }

  after {
    dir.destroy()
  }

  "update" should "fetch one migration file from src_migrations" in {
    implicit val wd = dir.testPath
    %sbt("mg init")
    %sbt("mg update")
    val file = new File(handled/"1.scala")
    val notExistsFile = new File(handled/"2.scala")
    assert(file.exists)
    assert(FileUtils.contentEquals(file, new File(unhandled/"1.scala")))
    assert(!notExistsFile.exists)
  }

  it should "fetch the earliest migration file that hasn't been applied" in {
    implicit val wd = dir.testPath
    %sbt("mg init", "mg update", "mg apply", "mg codegen")
    %sbt("mg update")
    val file = new File(handled/"2.scala")
    val notExistsFile = new File(handled/"3.scala")
    assert(file.exists)
    assert(FileUtils.contentEquals(file, new File(unhandled/"2.scala")))
    assert(!notExistsFile.exists)
  }

  "migrate" should "do update when there's no migration to apply" in {
    implicit val wd = dir.testPath
    %sbt("mg init")
    %sbt("mg migrate")
    val file = new File(handled/"1.scala")
    val notExistsFile = new File(handled/"2.scala")
    assert(file.exists)
    assert(FileUtils.contentEquals(file, new File(unhandled/"1.scala")))
    assert(!notExistsFile.exists)
  }

  "codegen" should "generate the code that later migrations can use" in {
    implicit val wd = dir.testPath
    %sbt("mg init", "mg update", "mg apply")
    %sbt("mg codegen", "mg update", "mg apply")
    val file = new File(
      testDir/Symbol("generated_code")/Symbol("src")/Symbol("main")/Symbol("scala")/Symbol("datamodel")/Symbol("v1")/Symbol("schema")/Symbol("schema.scala"))
    assert(file.exists)
  }

  def deleteMigrations() : Unit = {
    implicit val wd = dir.testPath
    ls! unhandled | rm
  }

  def testNewMigration(arg: String, deleteExistMigrations: Boolean = true) : Unit = {
    implicit val wd = dir.testPath
    if (deleteExistMigrations) {
      deleteMigrations()
      %sbt("mg init")
      %sbt(s"mg new $arg", "mg update", "mg apply")
      val file = new File(unhandled/"1.scala")
      assert(file.exists)
    } else {
      %sbt("mg init")
      var runArg = Seq(s"mg new $arg")
      for (i <- 0 until 4) runArg ++= Seq("mg update", "mg apply", "mg codegen")
      %sbt(runArg)
      val file = new File(unhandled/"5.scala")
      assert(file.exists)
    }
  }

  "new s" should "add a sql migration that can be applied" in {
    testNewMigration("s")
  }

  it should "add a sql migration that can be applied with existing files" in {
    testNewMigration("s", false)
  }

  "new sql" should "add a sql migration that can be applied" in {
    testNewMigration("sql")
  }

  it should "add a sql migration that can be applied with existing files" in {
    testNewMigration("sql", false)
  }

  "new d" should "add a dbio migration that can be applied" in {
    testNewMigration("d")
  }

  it should "add a dbio migration that can be applied with existing files" in {
    testNewMigration("d", false)
  }

  "new dbio" should "add a dbio migration that can be applied" in {
    testNewMigration("dbio")
  }

  it should "add a dbio migration that can be applied with existing files" in {
    testNewMigration("dbio", false)
  }

  def changeMigrationFile(filePath: Path,
    regex: util.matching.Regex, replacement: String)
    (implicit wd: Path) : Unit = {
    val lines = read.lines! filePath
    val content = lines.map { line =>
      regex.replaceAllIn(line, replacement)
    }.mkString("\n")
    rm(filePath)
    write(filePath, content)
  }

  def createTable(implicit wd: Path) : Unit = {
    deleteMigrations()
    %sbt("mg init")
    %sbt("mg new sql")
    val filePath = unhandled/"1.scala"
    assume(new File(filePath).exists)
    changeMigrationFile(filePath, "sqlu\".*\"".r,
      "sqlu\"\"\"create table \"coders\" (\"id\" INTEGER NOT NULL PRIMARY KEY,\"first\" VARCHAR NOT NULL,\"last\" VARCHAR NOT NULL)\"\"\"")
  }

  it should "work with a previous applied sql migration which creates a table" in {
    implicit val wd = dir.testPath
    createTable

    %sbt("mg new dbio")
    val filePath = unhandled/"2.scala"
    assume(new File(filePath).exists)
    changeMigrationFile(filePath, "DBIO.seq\\(.*\\)".r,
      """DBIO.seq(Coders ++= Seq(
        CodersRow(1, "Chris","Vogt"),
        CodersRow(2, "Yao","Li")""")
    %sbt("mg update", "mg apply", "mg codegen",
      "mg update", "mg apply", "mg codegen")
  }

  "new api" should "work with a previous applied sql migration which creates a table" in {
    implicit val wd = dir.testPath
    createTable

    %sbt("mg new api")
    val filePath = unhandled/"2.scala"
    assume(new File(filePath).exists)
    changeMigrationFile(filePath, "\\)\\(.*//.*\\)".r,
      """)(TableMigration(Coders).renameColumn(_.first, "firstname"))""")
    %sbt("mg update", "mg apply", "mg codegen",
      "mg update", "mg apply", "mg codegen")
  }
}
