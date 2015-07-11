package scala.migrations.slick

import scala.migrations.MigrationFilesHandler
import scala.migrations.RescueCommands
import scala.migrations.RescueCommandLineTool
import scala.migrations.MigrationCommands
import scala.migrations.MigrationCommandLineTool

import java.io.File

trait SlickMigrationFilesHandler extends MigrationFilesHandler[Int] {
  def nameIsId(name: String) =
    name forall Character.isDigit

  def nameToId(name: String): Int =
    name.toInt

  def idShouldBeHandled(id: String, appliedIds: Seq[Int]) =
    if (appliedIds.isEmpty) id.toInt == 1
    else id.toInt <= appliedIds.max + 1
}

trait SlickRescueCommands extends RescueCommands[Int]
    with SlickMigrationFilesHandler {
  this: SlickCodegen =>

  private def deleteRecursively(f: File) {
    if (f.isDirectory) {
      for {
        files <- Option(f.listFiles)
        file <- files
      } deleteRecursively(file)
    }
    f.delete
  }

  override def rescueCommand {
    super.rescueCommand
    deleteRecursively(new File(generatedDir))
  }
}

trait SlickRescueCommandLineTool extends RescueCommandLineTool[Int] {
  this: SlickRescueCommands =>
}

trait SlickMigrationCommands extends MigrationCommands[Int]
    with SlickMigrationFilesHandler {
  this: SlickMigrationManager with SlickCodegen =>

  override def applyCommands: Seq[() => Unit] = List(
    () => applyCommand, () => codegenCommand)


  override def statusCommand {
    val ny = notYetAppliedMigrations
    if( ny.size == 0 ) {
      println("your database is up-to-date")
    } else {
      println("your database is outdated, not yet applied migrations: "+notYetAppliedMigrations.map(_.id).mkString(", "))
    }
  }

  override def previewCommand {
    println("-" * 80)
    println("NOT YET APPLIED MIGRATIONS PREVIEW:")
    println("")
    notYetAppliedMigrations.map { migration =>
      migration match{
        case m:SqlMigration[_] =>
          println( migration.id+" SqlMigration:")
          println( "\t" + m.queries.mkString("\n\t") )
        case m:GenericMigration[_] =>
          println( migration.id+" GenericMigration:")
          println( "\t" + m.code )
      }
      println("")
    }
    println("-" * 80)
  }

  override def applyCommand {
    val ids = notYetAppliedMigrations.map(_.id)
    println("applying migrations: " + ids.mkString(", "))
    up
  }

  override def initCommand {
    super.initCommand
    init
  }

  override def resetCommand {
    super.resetCommand
    reset
  }

  def dbdumpCommand {
    import scala.slick.driver.H2Driver.simple._
    import Database.dynamicSession
    import scala.slick.jdbc.StaticQuery._
    db.withDynSession{
      println( queryNA[String]("SCRIPT").list.mkString("\n") )
    }
  }

  def codegenCommand {
    genCode(this)
  }
}


trait SlickMigrationCommandLineTool extends MigrationCommandLineTool[Int] {
  this: SlickMigrationCommands =>


  override def execCommands(args: List[String]) = args match {
    case "dbdump" :: Nil => dbdumpCommand
    case "codegen" :: Nil => codegenCommand
    case _ => super.execCommands(args)
  }

  override def help = super.help + """

  codegen   generate data model code (table objects, case classes) from the
            database schema

  dbdump    print a dump of the current database
"""
}
