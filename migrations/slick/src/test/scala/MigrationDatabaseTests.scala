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
    with BeforeAndAfter with ParallelTestExecution {
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

  def inDir(commands: Seq[String]) = {
    Process(commands, dir.dir)
  }

  "copy" should "commit db into .db" in {
    assert(inDir(Seq("git", "init")).! === 0)
    assert(inTestDir(Seq("sbt", "git-tools/run install")).! === 0)
    assert(inTestDir(Seq("sbt", "git-tools/run rebuild")).! === 0)
    assert(inDir(Seq("git", "add", ".")).! === 0)
    assert(inDir(Seq("git", "commit", "-m", "test")).! === 0)
    val dbFile = new File(objDir + "/master/db")
    assert(dbFile.exists === true)
    assert(dbFile.isFile === true)
    assert(FileUtils.contentEquals(dbFile,
      new File(dir.testPath + "/test.tb.h2.db")) === true)
  }
}
