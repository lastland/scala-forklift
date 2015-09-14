import org.scalatest._
import java.io.File
import java.nio.file.{Paths, Files, StandardCopyOption}
import org.apache.commons.io.FileUtils
import scala.sys.process._
import collection.mutable.ListBuffer
import com.liyaos.forklift.slick.tools.git.H2MigrationDatabase

class TestDir {
  val path = "tmp"
  val testPath = path + "/test"

  var dir: Option[File] = None
  var testDir: Option[File] = None

  def setup() {
    FileUtils.deleteDirectory(new File(path))
    val theDir = new File(path)
    if (!theDir.exists) theDir.mkdir()
    dir = Some(theDir)
    val theTestDir = new File(testPath)
    if (!theTestDir.exists) theTestDir.mkdir()
    testDir = Some(theTestDir)
    val example = new File(System.getProperty("user.dir") + "/example")
    FileUtils.copyDirectory(example, theTestDir)
  }

  override def toString = path
}

class MigrationDatabaseTest extends FlatSpec
    with BeforeAndAfter with GivenWhenThen {
  val dir = new TestDir
  val objDir = dir.testPath + "/.db"

  before {
    dir.setup()
    FileUtils.deleteDirectory(new File(objDir))
    assert(runInDir(Seq("git", "init")) === 0)
    assert(runInDir(Seq("git", "config", "user.email", "'test@test.com'")) === 0)
    assert(runInDir(Seq("git", "config", "user.name", "'Tester'")) === 0)
  }

  def inTestDir(commands: Seq[String]) = {
    dir.testDir match {
      case Some(testDir) =>
        Process(commands, testDir)
      case None =>
        throw new RuntimeException("the test dir has not been created yet!")
    }
  }

  def runInTestDir(commands: Seq[String]) = {
    inTestDir(commands) ! ProcessLogger(line => println(line),
      line => println(line))
  }

  def inDir(commands: Seq[String]) = {
    dir.dir match {
      case Some(dir) =>
        Process(commands, dir)
      case None =>
        throw new RuntimeException("the tmp dir has not been created yet!")
    }
  }

  def runInDir(commands: Seq[String]) = {
    inDir(commands) ! ProcessLogger(line => println(line),
      line => println(line))
  }

  "commit" should "copy db into .db on master branch" in {
    Given("an example project")
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
    assert(runInDir(Seq("git", "add", "test/build.sbt")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "initial")) === 0)
    assert(runInDir(Seq("git", "checkout", "-b", "test")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run install")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)
    val tmpFile = new File(dir.testPath + "/test_file")
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
    FileUtils.moveFile(new File(m3Name), new File(m3DisabledName))
    assert(runInTestDir(Seq("sbt", "git-tools/run install")) === 0)
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)
    for (dir <- dir.dir) {
      FileUtils.copyFileToDirectory(new File(".gitignore"), dir)
    }
    assert(runInDir(Seq("git", "add", ".")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "initial")) === 0)
    assert(runInDir(Seq("git", "checkout", "-b", "test")) === 0)
    FileUtils.moveFile(new File(m3DisabledName), new File(m3Name))
    assert(runInTestDir(Seq("sbt", "git-tools/run rebuild")) === 0)
    assert(runInDir(Seq("git", "add", ".")) === 0)
    assert(runInDir(Seq("git", "commit", "-m", "test")) === 0)
    assert(runInDir(Seq("git", "checkout", "master")) === 0)
    val tmpFile = new File(dir.testPath + "/test_file")
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
