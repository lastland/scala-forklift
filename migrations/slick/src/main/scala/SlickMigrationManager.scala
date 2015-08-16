package scala.migrations.slick

import java.io._
import com.typesafe.config._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable
import scala.migrations.Migration
import scala.migrations.MigrationManager

trait SlickMigrationManager
    extends MigrationManager[Int, slick.dbio.DBIO[Unit]] {
  val config = SlickMigrationsConfig.config

  import config.driver.api._

  class MigrationsTable(tag: Tag) extends Table[Int](tag, "__migrations__") {
    def id = column[Int]("id", O.PrimaryKey)
    def * = id
  }

  class DummyTable(tag: Tag, name: String) extends Table[Int](tag, name) {
    def id = column[Int]("id")
    def * = id
  }

  type SlickMigration = Migration[Int, DBIO[Unit]]

  val db = config.db

  lazy val migrationsTable = TableQuery[MigrationsTable]
  override def init = {
    val f = db.run(migrationsTable.schema.create)
    Await.result(f, Duration.Inf)
  }
  override def alreadyAppliedIds = {
    val f = db.run(migrationsTable.map(_.id).result)
    Await.result(f, Duration.Inf)
  }
  def latest = alreadyAppliedIds.last

  override protected def up(migrations: Iterator[SlickMigration]) = {
    val ups = DBIO.sequence(migrations flatMap { m =>
      List(m.up, migrationsTable += m.id)
    })
    val f = db.run(ups)
    Await.result(f, Duration.Inf)
  }

  override def reset = {
    val drop = MTable.getTables.flatMap { s =>
      DBIO.sequence(s map { t =>
        TableQuery(new DummyTable(_, t.name.name)).schema.drop
      })
    }
    val f = db.run(drop)
    try {
      (Glob.glob((f: File) => !f.isDirectory && f.getName.endsWith("schema.scala"))
        (List(System.getProperty("user.dir")+"/src/main/scala/datamodel/")))
        .foreach(_.delete)
    } catch {
      case e: FileNotFoundException =>
    }
    Await.result(f, Duration.Inf)
  }
}


object Glob{
  // taken from: http://kotakanbe.blogspot.ch/2010/11/scaladirglobsql.html
  def glob(filter: (File) => Boolean)(dirs: List[String]): List[File] = {
    def recursive(dir: File, acc: List[File]): List[File] =
      Option(dir.listFiles) match {
        case None => throw new FileNotFoundException(dir.getAbsolutePath)
        case Some(lists) =>
          val filtered = lists.filter{ c =>  filter(c) }.toList
          val childDirs = lists.filter{ c => c.isDirectory && !c.getName.startsWith(".") }
          return ( (acc ::: filtered) /: childDirs){ (a, dir) => recursive(dir, a)}
      }
    dirs.flatMap{ d => recursive(new File(d), Nil)}
  }
}
