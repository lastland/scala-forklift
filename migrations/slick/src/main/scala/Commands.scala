package scala.migrations.slick

import scala.migrations.RescueCommands
import scala.migrations.RescueCommandLineTool
import scala.migrations.MigrationCommands
import scala.migrations.MigrationCommandLineTool

import java.io.File

trait SlickRescueCommands extends RescueCommands {
  this: SlickCodegen =>

  def rescueCommand {
    for {
      files <- Option(new File(generatedDir).listFiles)
      file <- files
    } file.delete
  }
}

trait SlickMigrationCommands extends MigrationCommands[Int] {
  this: SlickMigrationManager with SlickCodegen =>

  override def statusCommand {
    val ny = notYetAppliedMigrations
    if( ny.size == 0 )
      println("your database is up-to-date")
    else
      println("your database is outdated, not yet applied migrations: "+notYetAppliedMigrations.map(_.id).mkString(", "))
  }

  override def previewCommand {
    println("-" * 80)
    println("NOT YET APPLIED MIGRATIONS PREVIEW:")
    println("")
    notYetAppliedMigrations.map{
      migration =>
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
    println("applying migrations: "+notYetAppliedMigrations.map(_.id).mkString(", "))
    up
  }

  override def initCommand { init }

  override def resetCommand { reset }

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
