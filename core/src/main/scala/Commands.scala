package com.liyaos.forklift.core

import java.io.File
import java.nio.file.Files
import java.io.BufferedWriter
import java.io.FileWriter
import scala.io.StdIn

trait MigrationFilesHandler[T] {

  protected lazy val config = MigrationsConfig.config
  protected lazy val unhandledLoc =
    config.getString("migrations.unhandled_location")
  protected lazy val handledLoc =
    config.getString("migrations.handled_location")

  protected def getId(name: String): Option[String] =
    if (!name.endsWith(".scala")) None
    else Some(name.substring(0, name.length - 6))

  def nameIsId(name: String): Boolean
  def nameToId(name: String): T
  def idShouldBeHandled(id: String, appliedIds: Seq[T]): Boolean

  def migrationFiles(alreadyAppliedIds: => Seq[T]): Stream[File] = {
    val appliedMigrationIds = alreadyAppliedIds
    val unhandled = new File(unhandledLoc)
    assert(unhandled.isDirectory)
    val toMove: Seq[File] = for {
      file <- unhandled.listFiles
      name <- getId(file.getName)
      if nameIsId(name)
      if idShouldBeHandled(name, appliedMigrationIds)
      if !appliedMigrationIds.contains(nameToId(name))
    } yield file
    toMove.toStream
  }

  def handleMigrationFile(file: File, cflag: Boolean) {
    val target = new File(handledLoc + "/" + file.getName)
    val source = new File(unhandledLoc + "/" + file.getName).getAbsoluteFile.toPath
    if (!target.exists) {
      println("create target to " + target.toPath + " for " + source)
      if (cflag) {
        Files.copy(source, target.toPath)
      } else {
        Files.createSymbolicLink(target.toPath, source)
      }
    }
  }

  protected def summary: Seq[String] = {
    val handled = new File(handledLoc)
    assert(handled.isDirectory)
    for {
      file <- handled.listFiles
      name <- getId(file.getName)
      if nameIsId(name)
    } yield name
  }

  protected def writeSummary(ids: Seq[String]) {
    // need to sort the migration ids before writing them to
    // the Summary file, so they are read in and processed
    // in the correct order.
    val code = "object MigrationSummary {\n" + ids
      .sortWith(_.toInt < _.toInt)
      .map(n => "M" + n)
      .mkString("\n") + "\n}\n"
    val sumFile = new File(handledLoc + "/Summary.scala")

    if (!sumFile.exists) sumFile.createNewFile()
    val fw = new FileWriter(sumFile.getAbsoluteFile())
    val bw = new BufferedWriter(fw)
    bw.write(code)
    bw.close()
  }

  def resetMigrationFiles {
    writeSummary(List())
    val handled = new File(handledLoc)
    val migs = for {
      file <- handled.listFiles
      name <- getId(file.getName)
      if nameIsId(name)
    } yield file
    for (file <- migs) {
      file.delete
    }
  }
}

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
