import java.io.File
import java.nio.file.{Paths, Files, StandardCopyOption}
import com.typesafe.config._
import scala.migrations.MigrationDatabase
import scala.migrations.core.tools.{GitUtil => Git}

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

  private def findDirOfCommit(commitId: String): File = {
    getOrMkDir(objLoc)
    val head2 = commitId.substring(0, 2)
    val dir1 = objLoc + "/" + head2
    getOrMkDir(dir1)
    val rest = commitId.substring(2)
    val dir2 = dir1 + "/" + rest
    getOrMkDir(dir2)
  }

  private def dbNameOfCommit(commitId: String): String = {
    val dir = findDirOfCommit(commitId)
    dir.getAbsolutePath + "/db"
  }

  private def dbOfCommit(commitId: String): File = {
    new File(dbNameOfCommit(commitId))
  }

  def copy(commitId: String) {
    val db = new File(dbName)
    if (!db.isFile) {
      throw new RuntimeException("The DB must be a file!")
    }
    val dir = findDirOfCommit(commitId)
    val dbStream = Files.newInputStream(Paths.get(db.getAbsolutePath))
    Files.copy(dbStream, Paths.get(dbNameOfCommit(commitId)))
  }

  def use(mainBranchId: String) {
    val db = dbOfCommit(mainBranchId)
    val dbStream = Files.newInputStream(Paths.get(db.getAbsolutePath))
    Files.copy(dbStream, Paths.get(dbName), StandardCopyOption.REPLACE_EXISTING)
  }
}

object GitUtil {
  private val config = ConfigFactory.load()
  val dbLoc = config.getString("db.url")
  val objLoc = config.getString("db.version_control_dir")

  def main(args: Array[String]) {
    val db = new MyMigrationDatabase(dbLoc, objLoc)
    val tool = new Git(db)
    tool.run(args.toList)
  }
}
