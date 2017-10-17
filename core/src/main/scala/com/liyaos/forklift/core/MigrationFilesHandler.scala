package com.liyaos.forklift.core

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Files

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
