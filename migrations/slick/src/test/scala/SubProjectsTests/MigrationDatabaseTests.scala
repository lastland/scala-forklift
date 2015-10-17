package com.liyaos.forklift.slick.tests.subprojects

import org.scalatest._
import java.io.File
import scala.language.implicitConversions
import org.apache.commons.io.FileUtils
import ammonite.ops._

class MigrationDatabaseTest extends FlatSpec
    with BeforeAndAfter with GivenWhenThen {
  val wd = cwd
  val dir = TestDir.createTestDir(wd)
  val testDir = dir.testPath
  val objDir = testDir/".db"
  val filesToWrite = (0 until 5).map(x => testDir/("file" + x))
  val dbName = testDir/"test.tb.mv.db"

  implicit def pathToString(path: Path) = path.toString

  before {
    dir.setup()
    rm(objDir)
    implicit val wd = dir.path
    %%git 'init
    %%git('config, "user.email", "test@test.com")
    %%git('config, "user.name", "Testser")
    for (file <- filesToWrite) {
      write(file, "dummy file")
    }
  }

  after {
    dir.destroy()
  }

  val migrationIterNum = 5

  def rebuild(implicit wd: Path) {
    %sbt("mg reset")
    %sbt("mg init")
    %sbt((0 until migrationIterNum).map(_ =>"mg migrate"))
  }

  "commit" should "copy db into .db on master branch" in {
    implicit val wd = dir.testPath

    Given("an example project")
    assert((%sbt("git-tools/run install")) === 0)
    rebuild

    When("commit on master branch")
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "test")) === 0)

    Then("a db file should be stored in objDir")
    val dbFile = new File(objDir/'master/'db)
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)

    And("the stored db should be identical with current db")
    assert(FileUtils.contentEquals(dbFile,
      new File(dbName)) === true)
  }

  it should "copy db into .db on test branch" in {
    implicit val wd = dir.testPath

    Given("an example project")
    assert((%sbt("git-tools/run install")) === 0)

    When("commit on test branch")
    assert((%git("checkout", "-b", "test")) === 0)
    rebuild
    assert((%git("add", filesToWrite(0))) === 0)
    assert((%git("commit", "-m", "test")) === 0)

    Then("a db file should be stored in objDir")
    val dbFile = new File(objDir/'test/'db)
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)

    And("the stored db should be identical with current db")
    assert(FileUtils.contentEquals(dbFile,
      new File(dbName)) === true)
  }

  "checkout" should "use the db of the target branch" in {
    implicit val wd = dir.testPath

    Given("an example project with two branches and one commit on test branch")
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "initial")) === 0)
    assert((%git("checkout", "-b", "test")) === 0)
    assert((%sbt("git-tools/run install")) === 0)
    rebuild
    assert((%git("add", filesToWrite(0))) === 0)
    assert((%git("commit", "-m", "test")) === 0)
    assert((%git("checkout", "master")) === 0)
    rm! dbName

    When("checkout to test branch")
    assert((%git("checkout", "test")) === 0)

    Then("the db should be in the test directory")
    val db = new File(dbName)
    assert(db.exists === true)
    assert(db.isFile === true)

    And("the db is identical with that stored in .db")
    assert(FileUtils.contentEquals(db,
      new File(objDir + "/test/db")) === true)
  }

  "merge" should "use the db of first parent" in {
    implicit val wd = dir.testPath

    Given("an example project with two branches with different db")
    val sourcePath = dir.testPath/'migrations/'src_migrations/'main/'scala
    mv(sourcePath/"3.scala", sourcePath/"3.scala.swp")
    assert((%sbt("git-tools/run install")) === 0)
    rebuild
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "initial")) === 0)
    assert((%git("checkout", "-b", "test")) === 0)
    mv(sourcePath/"3.scala.swp", sourcePath/"3.scala")
    rebuild
    assert((%git("add", filesToWrite(0))) === 0)
    assert((%git("commit", "-m", "test")) === 0)
    assert((%git("checkout", "master")) === 0)
    assert((%git("add", filesToWrite(1))) === 0)
    assert((%git("commit", "-m", "master")) === 0)

    When("two branches are merged")
    assert((%git("merge", "test", "-m", "merge")) === 0)

    Then("the db should be in test directory")
    val db = new File(dbName)
    assert(db.exists === true)
    assert(db.isFile === true)

    And("the db is identical to that from master but different from that from test")
    assert(FileUtils.contentEquals(db,
      new File(objDir/'master/'db)) === true)
    assert(FileUtils.contentEquals(db,
      new File(objDir/'test/'db)) === false)
  }
}
