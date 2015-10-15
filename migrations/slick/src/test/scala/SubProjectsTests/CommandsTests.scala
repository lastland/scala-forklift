package com.liyaos.forklift.slick.tests.subprojects

import org.scalatest._
import java.io.File
import scala.language.implicitConversions
import org.apache.commons.io.FileUtils
import ammonite.ops._

class CommandsTest extends FlatSpec
    with BeforeAndAfter {
  val wd = cwd
  val dir = TestDir.createTestDir(wd)
  val testDir = dir.testPath

  implicit def pathToString(path: Path) = path.toString

  val unhandled = testDir/'migrations/'src_migrations/'main/'scala
  val handled = testDir/'migrations/'src/'main/'scala/'migrations

  before {
    dir.setup()
  }

  after {
    dir.destroy()
  }

  "update" should "fetch one migration file from src_migrations" in {
    implicit val wd = dir.testPath
    assume((%sbt("mg init")) === 0)
    assert((%sbt("mg update")) === 0)
    val file = new File(handled/"1.scala")
    val notExistsFile = new File(handled/"2.scala")
    assert(file.exists)
    assert(FileUtils.contentEquals(file, new File(unhandled/"1.scala")))
    assert(!notExistsFile.exists)
  }

  it should "fetch the earliest migration file that hasn't been applied" in {
    implicit val wd = dir.testPath
    assume((%sbt("mg init", "mg update", "mg apply", "mg codegen")) === 0)
    assert((%sbt("mg update")) === 0)
    val file = new File(handled/"2.scala")
    val notExistsFile = new File(handled/"3.scala")
    assert(file.exists)
    assert(FileUtils.contentEquals(file, new File(unhandled/"2.scala")))
    assert(!notExistsFile.exists)
  }

  "migrate" should "do update when there's no migration to apply" in {
    implicit val wd = dir.testPath
    assume((%sbt("mg init")) === 0)
    assert((%sbt("mg migrate")) === 0)
    val file = new File(handled/"1.scala")
    val notExistsFile = new File(handled/"2.scala")
    assert(file.exists)
    assert(FileUtils.contentEquals(file, new File(unhandled/"1.scala")))
    assert(!notExistsFile.exists)
  }

  "codegen" should "generate the code that later migrations can use" in {
    implicit val wd = dir.testPath
    assume((%sbt("mg init", "mg update", "mg apply")) === 0)
    assert((%sbt("mg codegen", "mg update", "mg apply")) === 0)
    val file = new File(
      testDir/'generated_code/'src/'main/'scala/'datamodel/'v1/'schema/"schema.scala")
    assert(file.exists)
  }

  def testNewMigration(arg: String, deleteExistMigrations: Boolean = true) {
    implicit val wd = dir.testPath
    if (deleteExistMigrations) {
      ls! unhandled | rm
      assume((%sbt("mg init")) === 0)
      assert((%sbt(s"mg new $arg", "mg update", "mg apply")) === 0)
      val file = new File(unhandled/"1.scala")
      assert(file.exists)
    } else {
      assume((%sbt("mg init")) === 0)
      var runArg = Seq(s"mg new $arg")
      for (i <- 0 until 4) runArg ++= Seq("mg update", "mg apply", "mg codegen")
      assert((%sbt(runArg)) === 0)
      val file = new File(unhandled/"4.scala")
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
}
