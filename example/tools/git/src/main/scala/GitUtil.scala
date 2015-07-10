import java.io.File
import java.nio.file.{Paths, Files, StandardCopyOption}
import com.typesafe.config._
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

  def rebuild() {
    Seq("sbt", "mg reset").!
    Seq("sbt", "mg init").!
    Seq("sbt", "~mg migrate").!
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
