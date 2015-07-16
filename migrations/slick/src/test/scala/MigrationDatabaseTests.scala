import org.scalatest._
import java.io.File
import java.nio.file.{Paths, Files, StandardCopyOption}
import org.apache.commons.io.FileUtils
import scala.sys.process._
import collection.mutable.ListBuffer
import scala.migrations.slick.tools.git.H2MigrationDatabase

case class TestDir(path: String) {
  val testPath = path + "/test"

  lazy val dir = {
    val file = new File(path)
    if (!file.exists) file.mkdir()
    file
  }

  lazy val testDir = {
    dir
    val file = new File(testPath)
    if (!file.exists) file.mkdir()
    file
  }

  def setup() {
    val examples = new File(System.getProperty("user.dir") + "/example")
    FileUtils.copyDirectory(examples, testDir)
  }

  def clean() {
    FileUtils.deleteDirectory(new File(path))
  }

  override def toString = path
}

object TestDir {
  var counter = 0
  def getDir = synchronized {
    counter += 1
    TestDir("tmp" + counter)
  }
}

class MigrationDatabaseTest extends FlatSpec
    with BeforeAndAfter with ParallelTestExecution with GivenWhenThen {
  val dir = TestDir.getDir
  val objDir = dir.testPath + "/.db"

  before {
    assert(dir.testDir.isDirectory === true)
    dir.setup()
    FileUtils.deleteDirectory(new File(objDir))
  }

  after {
    dir.clean()
  }

  def inTestDir(commands: Seq[String]) = {
    Process(commands, dir.testDir)
  }

  def runInTestDir(commands: Seq[String]) = {
    inTestDir(commands) ! ProcessLogger(line => (), line => println(line))
  }

  def inDir(commands: Seq[String]) = {
    Process(commands, dir.dir)
  }

  def runInDir(commands: Seq[String]) = {
    inDir(commands) ! ProcessLogger(line => (), line => println(line))
  }

  "copy" should "commit db into .db on master branch" in {
    Given("an example project")
    assert(runInDir(Seq("git", "init")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run install")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)

    When("commit on master branch")
    assert(runInDir(Seq("git", "add", ".")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "test")) === 0)

    Then("a db file should be stored in objDir")
    val dbFile = new File(objDir + "/master/db")
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)

    And("the stored db should be identical with current db")
    assert(FileUtils.contentEquals(dbFile,
      new File(dir.testPath + "/test.tb.h2.db")) === true)
  }

  it should "commit db into .db on test branch" in {
    Given("an example project")
    assert(runInDir(Seq("git", "init")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run install")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)

    When("commit on master branch")
    assert(runInDir(Seq("git", "checkout", "-b", "test")) === 0)
    assert(runInDir(Seq("git", "add", ".")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "test")) === 0)

    Then("a db file should be stored in objDir")
    val dbFile = new File(objDir + "/test/db")
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)

    And("the stored db should be identical with current db")
    assert(FileUtils.contentEquals(dbFile,
      new File(dir.testPath + "/test.tb.h2.db")) === true)
  }
}
