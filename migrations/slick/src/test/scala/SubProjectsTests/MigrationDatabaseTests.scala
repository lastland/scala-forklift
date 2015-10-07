package com.liyaos.forklift.slick.tests.subprojects

import org.scalatest._
import java.io.File
import scala.language.implicitConversions
import org.apache.commons.io.FileUtils
import collection.mutable.ListBuffer
import ammonite.ops._
import com.liyaos.forklift.slick.tools.git.H2MigrationDatabase

class MigrationDatabaseTest extends FlatSpec
    with BeforeAndAfter with GivenWhenThen {
  val wd = cwd
  val dir = TestDir.createTestDir(wd)
  val testDir = dir.testPath
  val objDir = testDir/".db"

  implicit def pathToString(path: Path) = path.toString

  before {
    dir.setup()
    rm(objDir)
    implicit val wd = dir.path
    %%git 'init
    %%git('config, "user.email", "test@test.com")
    %%git('config, "user.name", "Testser")
  }

  after {
    dir.destroy()
  }

  "commit" should "copy db into .db on master branch" in {
    implicit val wd = dir.testPath

    Given("an example project")
    assert((%sbt("git-tools/run install")) === 0)
    assert((%sbt("git-tools/run rebuild")) === 0)

    When("commit on master branch")
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "test")) === 0)

    Then("a db file should be stored in objDir")
    val dbFile = new File(objDir/'master/'db)
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)

    And("the stored db should be identical with current db")
    assert(FileUtils.contentEquals(dbFile,
      new File(dir.testPath/"test.tb.h2.db")) === true)
  }

  it should "copy db into .db on test branch" in {
    implicit val wd = dir.testPath

    Given("an example project")
    assert((%sbt("git-tools/run install")) === 0)
    assert((%sbt("git-tools/run rebuild")) === 0)

    When("commit on master branch")
    assert((%git("checkout", "-b", "test")) === 0)
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "test")) === 0)

    Then("a db file should be stored in objDir")
    val dbFile = new File(objDir/'test/'db)
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)

    And("the stored db should be identical with current db")
    assert(FileUtils.contentEquals(dbFile,
      new File(dir.testPath/"test.tb.h2.db")) === true)
  }

  "checkout" should "use the db of the target branch" in {
    implicit val wd = dir.testPath

    Given("an example project with two branches and one commit on test branch")
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "initial")) === 0)
    assert((%git("checkout", "-b", "test")) === 0)
    assert((%sbt("git-tools/run install")) === 0)
    assert((%sbt("git-tools/run rebuild")) === 0)
    write(wd/"test_file", "Hello World!")
    assert((%git("add", wd/"test_file")) === 0)
    assert((%git("commit", "-m", "test")) === 0)
    assert((%git("checkout", "master")) === 0)
    rm! dir.testPath/"test.tb.h2.db"

    When("checkout to test branch")
    assert((%git("checkout", "test")) === 0)

    Then("the db should be in the test directory")
    val db = new File(dir.testPath + "/test.tb.h2.db")
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
    assert((%sbt("git-tools/run rebuild")) === 0)
    assert((%git("add", "build.sbt")) === 0)
    assert((%git("commit", "-m", "initial")) === 0)
    assert((%git("checkout", "-b", "test")) === 0)
    mv(sourcePath/"3.scala.swp", sourcePath/"3.scala")
    assert((%sbt("git-tools/run rebuild")) === 0)
    assert((%git("add", sourcePath/"3.scala")) === 0)
    assert((%git("commit", "-m", "test")) === 0)
    assert((%git("checkout", "master")) === 0)
    write(wd/"test_file", "Hello World!")
    assert((%git("add", wd/"test_file")) === 0)
    assert((%git("commit", "-m", "master")) === 0)

    When("two branches are merged")
    assert((%git("merge", "test", "-m", "merge")) === 0)

    Then("the db should be in test directory")
    val db = new File(dir.testPath/"test.tb.h2.db")
    assert(db.exists === true)
    assert(db.isFile === true)

    And("the db is identical to that from master but different from that from test")
    assert(FileUtils.contentEquals(db,
      new File(objDir/'master/'db)) === true)
    assert(FileUtils.contentEquals(db,
      new File(objDir/'test/'db)) === false)
  }
}
