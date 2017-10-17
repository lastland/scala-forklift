package com.liyaos.forklift.core

import scala.io.StdIn



trait RescueCommands[T] {
  this: MigrationFilesHandler[T] =>
  def rescueCommand {
    resetMigrationFiles
  }
}

trait MigrationCommands[T, S] {
  this: MigrationManager[T, S] with MigrationFilesHandler[T] =>

  def previewOps: Seq[() => Unit] = List(() => previewOp)
  def applyOps: Seq[() => Unit] = List(() => applyOp)

  def statusOp: Unit
  def statusCommand {
    statusOp
  }

  def previewOp: Unit
  def previewCommand {
    previewOp
  }

  def applyOp: Unit
  def applyCommand {
    applyOp
  }

  def migrateOp(options: Seq[String]) {
    val prompt = options.contains("-p")
    val cflag = options.contains("-c")
    if (!notYetAppliedMigrations.isEmpty) {
      for (op <- previewOps) op()
      if (prompt) {
        if (StdIn.readLine("Do you wish to continue? [Y/N]") != "Y") return
      }
      for (op <- applyOps) op()
    }
    updateOp(cflag)
  }
  def migrateCommand(options: Seq[String]) {
    migrateOp(options)
  }

  def initOp {
    writeSummary(List())
  }
  def initCommand {
    initOp
  }

  def resetOp {
    resetMigrationFiles
  }
  def resetCommand {
    resetOp
  }

  def updateOp(cflag: Boolean = false) {
    val files = migrationFiles(alreadyAppliedIds)
    if (!files.isEmpty) {
      for (file <- files) {
        handleMigrationFile(file, cflag)
      }
      // only write summary if files are moved
      writeSummary(summary)
    }
  }
  def updateCommand(options: Seq[String]) {
    val cflag = options.contains("-c")
    updateOp(cflag)
  }
}

trait RescueCommandLineTool[T] { this: RescueCommands[T] =>
  def execCommands(args: List[String]) = args match {
    case "rescue" :: Nil => rescueCommand
    case _               => println("Unknown commands")
  }
}

// TODO: This trait may need to be rewritten in the future.
// e.g.: use macros etc. to make adding command (and help info) easier
trait MigrationCommandLineTool[T, S] { this: MigrationCommands[T, S] =>

  def execCommands(args: List[String]) = args match {
    case "status" :: Nil                    => statusCommand
    case "preview" :: Nil                   => previewCommand
    case "apply" :: Nil                     => applyCommand
    case "init" :: Nil                      => initCommand
    case "reset" :: Nil                     => resetCommand
    case "update" :: (options: Seq[String]) => updateCommand(options)
    case "migrate" :: (options: Seq[String]) =>
      migrateCommand(options)
    case _ => println(helpOutput)
  }

  def helpOutput =
    List("-" * 80, "A list of command available in this proof of concept:", help, "-" * 80)
      .mkString("\n")

  // TODO: This may need to be rewritten in the future.
  def help = """
  init      create the __migrations__ table which stores version information

  reset     totally clears the database and deletes auto-generated source files
            (this can be used to restart the demo and start again with init)

  migrate   automatically fetch migrations, preview and apply them

  status    display the migrations that have not been applied yet

  update    fetch migrations to apply

  preview   display the migrations that have not been applied yet and show
            corresponding sql for sql migrations

  apply     apply all migrations which have not been applied yet
            (this could be extended to allow applying only migrations up to a
             stated migration but not further)
"""
}
