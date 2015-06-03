package scala.migrations

import java.io.File
import java.nio.file.Files

trait RescueCommands {
  def rescueCommand: Unit
}

trait MigrationCommands[T] { this: MigrationManager[T] =>
  def appliedMigrationIds = alreadyAppliedIds

  def statusCommand: Unit
  def previewCommand: Unit
  def applyCommand: Unit
  def initCommand: Unit
  def resetCommand: Unit
}

trait RescueCommandLineTool { this: RescueCommands =>
  def execCommands(args: List[String]) = args match {
    case "rescue" :: Nil => rescueCommand
    case _ => println("Unknown commands")
  }
}

// TODO: This trait may need to be rewritten in the future.
// e.g.: use macros etc. to make adding command (and help info) easier
trait MigrationCommandLineTool[T] { this: MigrationCommands[T] =>
  private lazy val config = MigrationsConfig.config
  protected lazy val unhandledLoc =
    config.getString("migrations.unhandled_location")
  protected lazy val handledLoc =
    config.getString("migrations.handled_location")

  def idShouldBeHandled(id: String, appliedIds: Seq[T]): Boolean
  def handleMigrations {
    val unhandled = new File(unhandledLoc)
    assert(unhandled.isDirectory)
    val toMove: Seq[File] = for {
      file <- unhandled.listFiles
      fullName = file.getName
      if fullName.endsWith(".scala")
      name = fullName.substring(0, fullName.length - 6)
      if name forall Character.isDigit
      if idShouldBeHandled(name, appliedMigrationIds)
    } yield file

    for (file <- toMove) {
      val link = new File(handledLoc + "/" + file.getName)
      val target = new File(
        unhandledLoc + "/" + file.getName).getAbsoluteFile.toPath
      if (!link.exists) {
        println("create link to " + link.toPath + " for " + target)
        Files.createSymbolicLink(link.toPath, target)
      }
    }

    val ids = toMove.map(n => n.getName.substring(0, n.getName.length - 6))
    val code = "object MigrationSummary {\n" + ids.map(
      n => "M" + n).mkString("\n") + "\n}\n"
    val sumFile = new File(handledLoc + "/Summary.scala")

    if (!sumFile.exists) sumFile.createNewFile()
    import java.io.BufferedWriter
    import java.io.FileWriter
    val fw = new FileWriter(sumFile.getAbsoluteFile())
    val bw = new BufferedWriter(fw)
    bw.write(code)
    bw.close()
  }

  def execCommands(args: List[String]) = args match {
    case "status" :: Nil => statusCommand
    case "preview" :: Nil => previewCommand
    case "apply" :: Nil => applyCommand
    case "init" :: Nil => initCommand
    case "reset" :: Nil => resetCommand
    case "update" :: Nil => handleMigrations
    case _ => println(helpOutput)
  }

  def helpOutput = List("-" * 80,
    "A list of command available in this proof of concept:",
    help, "-" * 80).mkString("\n")

  // TODO: This may need to be rewritten in the future.
  def help = """
  init      create the __migrations__ table which stores version information

  reset     totally clears the database and deletes auto-generated source files
            (this can be used to restart the demo and start again with init)

  status    display the migrations that have not been applied yet

  preview      display the migrations that have not been applied yet and show
            corresponding sql for sql migrations

  apply     apply all migrations which have not been applied yet
            (this could be extended to allow applying only migrations up to a
             stated migration but not further)
"""
}
