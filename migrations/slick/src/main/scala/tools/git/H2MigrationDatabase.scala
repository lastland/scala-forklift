package scala.migrations.slick.tools.git
import java.io.File
import scala.sys.process._
import java.nio.file.{Paths, Files, StandardCopyOption}
import scala.migrations.core.tools.helpers.Helpers._
import scala.migrations.MigrationDatabase

class H2MigrationDatabase(dbLoc: String, objLoc: String) extends MigrationDatabase {

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
    Seq("sbt", "~mg migrate").!->
  }

  def rebuild(branch: String, commitId: String) {
    rebuild()
    copy(branch, commitId)
  }
}
