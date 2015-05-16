package scala.migrations

trait RescueCommands {
  def rescueCommand: Unit
}

trait MigrationCommands[T] { this: MigrationManager[T] =>
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

  def execCommands(args: List[String]) = args match {
    case "status" :: Nil => statusCommand
    case "preview" :: Nil => previewCommand
    case "apply" :: Nil => applyCommand
    case "init" :: Nil => initCommand
    case "reset" :: Nil => resetCommand
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
