package com.liyaos.forklift.slick

import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import scala.util.{ Failure, Success }
import com.liyaos.forklift.core.MigrationsConfig
import com.liyaos.forklift.core.MigrationFilesHandler
import com.liyaos.forklift.core.RescueCommands
import com.liyaos.forklift.core.RescueCommandLineTool
import com.liyaos.forklift.core.MigrationCommands
import com.liyaos.forklift.core.MigrationCommandLineTool

object CommandExceptions {
  case class WrongNumberOfArgumentsException(expected: Int, given: Int)
      extends Exception(s"We expect $expected arguments, but we find $given.")
  case class WrongArgumentException(arg: String)
      extends Exception(s"We do not understand the argument you give: $arg.")
}

object MigrationType extends Enumeration {
  import CommandExceptions._

  type MigrationType = Value
  val SQL, DBIO, API = Value

  val nameMap = Map("sql" -> SQL, "s" -> SQL, "dbio" -> DBIO, "d" -> DBIO, "api" -> API, "a" -> API)

  def getType(s: String) = nameMap.get(s.toLowerCase)
}

trait SlickMigrationFilesHandler extends MigrationFilesHandler[Int] {
  def nameIsId(name: String) =
    name forall Character.isDigit

  def nameToId(name: String): Int =
    name.toInt

  def idShouldBeHandled(id: String, appliedIds: Seq[Int]) =
    if (appliedIds.isEmpty) id.toInt == 1
    else id.toInt <= appliedIds.max + 1

  def nextId = {
    val unhandled = new File(unhandledLoc)
    val ids = for {
      file <- unhandled.listFiles
      name <- getId(file.getName)
      if nameIsId(name)
    } yield nameToId(name)
    if (!ids.isEmpty) ids.max + 1 else 1
  }
}

trait SlickRescueCommands extends RescueCommands[Int] with SlickMigrationFilesHandler {
  this: SlickCodegen =>

  private def deleteRecursively(f: File) : Unit = {
    if (f.isDirectory) {
      for {
        files <- Option(f.listFiles)
        file <- files
      } deleteRecursively(file)
    }
    f.delete
  }

  override def rescueCommand : Unit = {
    super.rescueCommand
    deleteRecursively(new File(generatedDir))
  }
}

trait SlickRescueCommandLineTool extends RescueCommandLineTool[Int] {
  this: SlickRescueCommands =>
}

trait SlickMigrationCommands
    extends MigrationCommands[Int, slick.dbio.DBIO[Unit]]
    with SlickMigrationFilesHandler {
  this: SlickMigrationManager with SlickCodegen =>

  import MigrationType._
  import OptionToTry._

  override def applyOps: Seq[() => Unit] = List(() => applyOp, () => codegenOp)

  override def statusOp : Unit = {
    val mf = migrationFiles(alreadyAppliedIds)
    if (!mf.isEmpty) {
      println("you still have unhandled migrations")
      println("use mg update to fetch these migrations")
    } else {
      val ny = notYetAppliedMigrations
      if (ny.size == 0) {
        println("your database is up-to-date")
      } else {
        println(
          "your database is outdated, not yet applied migrations: " + notYetAppliedMigrations
            .map(_.id)
            .mkString(", ")
        )
      }
    }
  }

  override def statusCommand : Unit = {
    try {
      super.statusCommand
    } finally {
      db.close()
    }
  }

  override def previewOp : Unit = {
    println("-" * 80)
    println("NOT YET APPLIED MIGRATIONS PREVIEW:")
    println("")
    notYetAppliedMigrations.map { migration =>
      migration match {
        case m: SqlMigrationInterface[_] =>
          println(s"${migration.id} SqlMigration:")
          println("\t" + m.queries.map(_.getDumpInfo.mainInfo).mkString("\n\t"))
        case m: SqlResourceMigrationInterface[_] =>
          println(s"${migration.id} SqlResourceMigration:")
          println(m.sqlQueries)
        case m: DBIOMigration[_] =>
          println(s"${migration.id} DBIOMigration:")
          println("\t" + m.code)
        case m: APIMigration[_] =>
          println(s"${migration.id} APIMigration:")
          println("\t" + m.migration.sql)
      }
      println("")
    }
    println("-" * 80)
  }

  override def previewCommand : Unit = {
    try {
      super.previewCommand
    } finally {
      db.close()
    }
  }

  override def applyOp : Unit = {
    val ids = notYetAppliedMigrations.map(_.id)
    println("applying migrations: " + ids.mkString(", "))
    up()
  }

  override def applyCommand : Unit = {
    try {
      super.applyCommand
    } finally {
      db.close()
    }
  }

  override def migrateCommand(options: Seq[String]) : Unit = {
    try {
      super.migrateCommand(options)
    } finally {
      db.close()
    }
  }

  override def initOp : Unit = {
    super.initOp
    init
  }

  override def initCommand : Unit = {
    try {
      super.initCommand
    } finally {
      db.close()
    }
  }

  override def resetOp : Unit = {
    super.resetOp
    reset
    remove()
  }

  override def resetCommand : Unit = {
    try {
      super.resetCommand
    } finally {
      db.close()
    }
  }

  override def updateCommand(options: Seq[String]) : Unit = {
    try super.updateCommand(options)
    finally {
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

  def addMigrationOp(tpe: MigrationType, version: Int) : Unit = {
    val migrationObject = config.getString("migrations.migration_object")
    val driverName = dbConfig.profileName
    val dbName = driverName.substring("slick.jdbc.".length, driverName.length - "Profile".length)
    val imports =
      if (version > 1)
        s"""import ${pkgName("v" + (version - 1))}.tables._"""
      else ""
    val content = tpe match {
      case SQL => s"""import ${driverName}.api._
import com.liyaos.forklift.slick.SqlMigration

object M${version} {
  ${migrationObject}.migrations = ${migrationObject}.migrations :+ SqlMigration(${version})(List(
    sqlu"" // your sql code goes here
  ))
}
"""
      case DBIO =>
        s"""import ${driverName}.api._
import com.liyaos.forklift.slick.DBIOMigration
${imports}

object M${version} {
  ${migrationObject}.migrations = ${migrationObject}.migrations :+ DBIOMigration(${version})(
    DBIO.seq(
      // write your dbio actions here
    ))
}
"""
      case API =>
        s"""import ${driverName}.api._
import slick.migration.api.TableMigration
import slick.migration.api.${dbName}Dialect
import com.liyaos.forklift.slick.APIMigration
${imports}

object M${version} {
  implicit val dialect = new ${dbName}Dialect

  ${migrationObject}.migrations = ${migrationObject}.migrations :+ APIMigration(${version})(// write your migration here)
}"""
    }
    val file = new File(unhandledLoc + "/" + version + ".scala")
    if (!file.exists) file.createNewFile()
    val bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()))
    bw.write(content)
    bw.close()
  }

  def addMigrationCommand(options: Seq[String]) : Unit = {
    val tpe = options.headOption.toTry(CommandExceptions.WrongNumberOfArgumentsException(1, 0)) flatMap {
      s =>
        MigrationType.getType(s).toTry(CommandExceptions.WrongArgumentException(s))
    }
    val t = tpe.get
    addMigrationOp(t, nextId)
  }

  def codegenOp : Unit = {
    genCode(this)
  }

  def codegenCommand : Unit = {
    try {
      codegenOp
    } finally {
      db.close()
    }
  }
}

trait SlickMigrationCommandLineTool extends MigrationCommandLineTool[Int, slick.dbio.DBIO[Unit]] {
  this: SlickMigrationCommands =>

  override def execCommands(args: List[String]) = args match {
    case "codegen" :: Nil => codegenCommand
    case "new" :: tpe     => addMigrationCommand(tpe)
    case _                => super.execCommands(args)
  }

  override def help =
    super.help + """
  codegen   generate data model code (table objects, case classes) from the
            database schema

  new       create a new migration file. please specify migration type with
            "s" (sql) or "d" (dbio)
"""
}
