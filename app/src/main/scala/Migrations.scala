import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery._
import java.io._
import scala.migrations.Migration
import scala.migrations.slick.MigrationsTable
import scala.migrations.slick.SlickMigrationManager

trait MyMigrationManager extends SlickMigrationManager[Int]{
  val migrationsTable = TableQuery[MigrationsTable]
  def init = db.withDynSession(migrationsTable.ddl.create)
  def alreadyAppliedIds = db.withDynSession{migrationsTable.map(_.id).list}
  def latest = alreadyAppliedIds.last
  def afterApply(migration:Migration[Int]) = {
    db.withDynSession{migrationsTable.insert( migration.id )}
  }
  override def beforeApply(migration:Migration[Int]) = {
    println("applying migration "+migration.id)
  }
  override def up : Unit = {
    val ids = migrations.map(_.id)
    assert( ids == Range(1,ids.size+1).toList )
    super.up
  }
  def reset(){
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
