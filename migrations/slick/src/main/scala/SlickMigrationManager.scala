package scala.migrations.slick

import java.io._
import com.typesafe.config._
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery._
import scala.migrations.Migration
import scala.migrations.MigrationManager

class MigrationsTable(tag: Tag) extends Table[Int](tag, "__migrations__") {
  def id = column[Int]("id", O.PrimaryKey)
  def * = id
}

trait SlickMigrationManager extends MigrationManager[Int] {
  private val config = ConfigFactory.load()
  protected val dburl = config.getString("migrations.db.url")
  protected val dbdriver = config.getString("migrations.db.driver")
  lazy val db = Database.forURL(dburl, driver = dbdriver)
  override def init = db.withDynSession(migrationsTable.ddl.create)
  override def alreadyAppliedIds =
    db.withDynSession{migrationsTable.map(_.id).list}
  override def latest = alreadyAppliedIds.last
  val migrationsTable = TableQuery[MigrationsTable]

  override def up {
    db.withDynSession {
      super.up
    }
  }

  override def singleUp {
    db.withDynTransaction {
      super.singleUp
    }
  }

  override def rollback {
    dynamicSession.rollback
  }

  override def afterApply(migration: Migration[Int]) = {
    db.withDynSession{migrationsTable.insert( migration.id )}
  }

  override def reset() {
    db.withDynSession{updateNA("DROP ALL OBJECTS DELETE FILES").execute}
    try{
      (Glob.glob((f: File) => !f.isDirectory && f.getName.endsWith("schema.scala"))
        (List(System.getProperty("user.dir")+"/src/main/scala/datamodel/")))
        .foreach(_.delete)
    }catch{
      case e:FileNotFoundException =>
    }
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
