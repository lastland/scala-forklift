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

  "commit" should "copy db into .db on master branch" in {
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

  it should "copy db into .db on test branch" in {
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

  "checkout" should "use the db of the target branch" in {
    Given("an example project with two branches and one commit on test branch")
    assert(runInDir(Seq("git", "init")) === 0)
    assert(runInDir(Seq("git", "add", "test/build.sbt")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "initial")) === 0)
    assert(runInDir(Seq("git", "checkout", "-b", "test")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run install")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)
    val tmpFile = new File(dir.testDir + "/test_dir")
    tmpFile.createNewFile()
    assert(runInDir(Seq("git", "add", tmpFile.getAbsolutePath)) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "test")) === 0)
    assert(runInDir(Seq("git", "checkout", "master")) === 0)
    new File(dir.testPath + "/test.tb.h2.db").delete()

    When("checkout to test branch")
    assert(runInDir(Seq("git", "checkout", "test")) === 0)

    Then("the db should be in the test directory")
    val db = new File(dir.testPath + "/test.tb.h2.db")
    assert(db.exists === true)
    assert(db.isFile === true)

    And("the db is identical with that stored in .db")
    assert(FileUtils.contentEquals(db,
      new File(objDir + "/test/db")) === true)
  }

  "merge" should "use the db of first parent" in {
    val m3Name = dir.testPath + "/migrations/src_migrations/main/scala/3.scala"
    val m3DisabledName = m3Name + ".disabled"
    Given("an example project with two branches with different db")
    assert(runInDir(Seq("git", "init")) === 0)
    FileUtils.moveFile(new File(m3Name), new File(m3DisabledName))
    assert(runInTestDir(Seq("sbt", "git-tools/run install")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)
    FileUtils.copyFileToDirectory(new File(".gitignore"), dir.dir)
    assert(runInDir(Seq("git", "add", ".")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "initial")) === 0)
    assert(runInDir(Seq("git", "checkout", "-b", "test")) === 0)
    FileUtils.moveFile(new File(m3DisabledName), new File(m3Name))
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)
    assert(runInDir(Seq("git", "add", ".")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "test")) === 0)
    assert(runInDir(Seq("git", "checkout", "master")) === 0)
    val tmpFile = new File(dir.testDir + "/test_file")
    tmpFile.createNewFile()
    assert(runInDir(Seq("git", "add", tmpFile.getAbsolutePath)) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "master")) === 0)

    When("two branches are merged")
    assert(runInDir(Seq("git", "merge", "test")) === 0)

    Then("the db should be in test directory")
    val db = new File(dir.testPath + "/test.tb.h2.db")
    assert(db.exists === true)
    assert(db.isFile === true)

    And("the db is identical to that from master but different from that from test")
    assert(FileUtils.contentEquals(db,
      new File(objDir + "/master/db")) === true)
    assert(FileUtils.contentEquals(db,
      new File(objDir + "/test/db")) === false)
  }
}
