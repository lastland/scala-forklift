import java.io.File
import java.io.{InputStream, OutputStream}
import java.nio.file.{Paths, Files, StandardCopyOption}
import com.typesafe.config._
import scala.util.{Try, Success, Failure}
import scala.annotation.tailrec
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import scala.language.postfixOps
import scala.sys.process._
import scala.migrations.MigrationDatabase
import scala.migrations.core.tools.{GitUtil => Git}
import scala.migrations.tools.git.Installer

class MyMigrationDatabase(dbLoc: String, objLoc: String)
    extends MigrationDatabase {

  lazy val dbName = {
    val prefix = "jdbc:h2:"
    dbLoc.substring(dbLoc.indexOfSlice(prefix) + prefix.length) + ".h2.db"
  }

  private def getOrMkDir(name: String): File = {
    val file = new File(name)
    if (!file.exists) file.mkdir()
    if (!file.isDirectory)
      throw new RuntimeException(s"$name must be a directory!")
    file
  }

  private def findBranchDir(branch: String): File = {
    getOrMkDir(objLoc)
    getOrMkDir(objLoc + "/" + branch)
  }

  private def dbNameOfBranch(branch: String): String = {
    findBranchDir(branch).getAbsolutePath + "/db"
  }

  def copy(branch: String, commitId: String) {
    val db = new File(dbName)
    if (!db.isFile) {
      throw new RuntimeException("The DB must be a file!")
    }
    val gitDbName = dbNameOfBranch(branch)
    val dbStream = Files.newInputStream(Paths.get(db.getAbsolutePath))
    Files.copy(dbStream, Paths.get(gitDbName), StandardCopyOption.REPLACE_EXISTING)
  }

  def use(branch: String, commitId: String) {
    val gitDbName = dbNameOfBranch(branch)
    val db = new File(gitDbName)
    if (!db.exists) {
      rebuild(branch, commitId)
    } else {
      val dbStream = Files.newInputStream(Paths.get(db.getAbsolutePath))
      Files.copy(dbStream, Paths.get(dbName), StandardCopyOption.REPLACE_EXISTING)
    }
  }

  private def runCommandUntilNoOutput(command: Seq[String]) {
    @tailrec def noOutput(flag: Boolean, input: InputStream, text: String): Unit = {
      var byte: Array[Byte] = new Array(1)
      val f = Future {
        if (input.read(byte) != -1) ()
      }
      val fl = text.endsWith("Waiting for source changes... (press enter to interrupt)\n")
      val duration = if (fl) 2 seconds else 30 seconds
      val r = Try { Await.result(f, duration) }
      val s = new String(byte)
      print(s)
      r match {
        case Success(_) => noOutput(fl, input, text + s)
        case Failure(ex) => ex match {
          case te: TimeoutException =>
            if (fl) input.close() else throw te
        }
      }
    }

    command run new ProcessIO(_.close(), noOutput(false, _, ""), _.close())
  }

  def rebuild() {
    Seq("sbt", "mg reset").!
    Seq("sbt", "mg init").!
    runCommandUntilNoOutput(Seq("sbt", "~mg migrate"))
  }

  def rebuild(branch: String, commitId: String) {
    rebuild()
    copy(branch, commitId)
  }
}

class MyGitUtil(db: MyMigrationDatabase)
    extends Git(db, System.getProperty("user.dir") + "/../.git") {
  override def run(args: List[String]) {
    args match {
      case "install" :: Nil =>
        val currentDir = System.getProperty("user.dir")
        Installer.install(currentDir + "/../.git", currentDir, "git-tools")
      case "rebuild" :: Nil =>
        db.rebuild()
      case _ =>
        super.run(args)
    }
  }
}

object GitUtil {
  private val config = ConfigFactory.load()
  val dbLoc = config.getString("db.url")
  val objLoc = config.getString("db.version_control_dir")

  def main(args: Array[String]) {
    val db = new MyMigrationDatabase(dbLoc, objLoc)
    val tool = new MyGitUtil(db)
    tool.run(args.toList)
  }
}
