package scala.migrations.slick

import slick.driver.JdbcDriver.api._
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

trait SlickMigrationCommands extends MigrationCommands[Int, DBIO[Unit]]
    with SlickMigrationFilesHandler {
  this: SlickMigrationManager with SlickCodegen =>

  override def applyOps: Seq[() => Unit] = List(
    () => applyOp, () => codegenOp)

  override def statusOp {
    val ny = notYetAppliedMigrations
    if( ny.size == 0 ) {
      println("your database is up-to-date")
    } else {
      println("your database is outdated, not yet applied migrations: "+notYetAppliedMigrations.map(_.id).mkString(", "))
    }
  }

  override def statusCommand {
    try {
      super.statusCommand
    } finally {
      db.close()
    }
  }

  override def previewOp {
    println("-" * 80)
    println("NOT YET APPLIED MIGRATIONS PREVIEW:")
    println("")
    notYetAppliedMigrations.map { migration =>
      migration match{
        case m: SqlMigrationInterface[_] =>
          println( migration.id + " SqlMigration:")
          println( "\t" + m.queries.map(_.getDumpInfo.mainInfo).mkString("\n\t") )
        case m: GenericMigration[_] =>
          println( migration.id + " GenericMigration:")
          println( "\t" + m.code )
      }
      println("")
    }
    println("-" * 80)
  }

  override def previewCommand {
    try {
      super.previewCommand
    } finally {
      db.close()
    }
  }

  override def applyOp {
    val ids = notYetAppliedMigrations.map(_.id)
    println("applying migrations: " + ids.mkString(", "))
    up()
  }

  override def applyCommand {
    try {
      super.applyCommand
    } finally {
      db.close()
    }
  }

  override def migrateCommand(options: Seq[String]) {
    try {
      super.migrateCommand(options)
    } finally {
      db.close()
    }
  }

  override def initOp {
    super.initOp
    init
  }

  override def initCommand {
    try {
      super.initCommand
    } finally {
      db.close()
    }
  }

  override def resetOp {
    super.resetOp
    reset
  }

  override def resetCommand {
    try {
      super.resetCommand
    } finally {
      db.close()
    }
  }

  override def updateCommand {
    try {
      super.updateCommand
    } finally {
      db.close()
    }
  }

//  def dbdumpCommand {
//    import scala.slick.driver.H2Driver.simple._
//    import Database.dynamicSession
//    import scala.slick.jdbc.StaticQuery._
//    db.withDynSession{
//      println( queryNA[String]("SCRIPT").list.mkString("\n") )
//    }
//  }

  def codegenOp {
    genCode(this)
  }

  def codegenCommand {
    try {
      codegenOp
    } finally {
      db.close()
    }
  }
}


trait SlickMigrationCommandLineTool
    extends MigrationCommandLineTool[Int, DBIO[Unit]] {
  this: SlickMigrationCommands =>

  override def execCommands(args: List[String]) = args match {
    case "codegen" :: Nil => codegenCommand
    case _ => super.execCommands(args)
  }

  override def help = super.help + """

  dbdump    print a dump of the current database
"""
}
