package scala.slick.migrations

import java.io._
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

trait Migration[T]{
  def id : T
  def up : Unit
}
import scala.slick.jdbc.StaticQuery._
trait SqlMigrationInterface[T] extends Migration[T]{
  def queries : Seq[String]
  def up{
    queries.foreach(updateNA(_).execute)
  }
}
case class SqlMigration[T] (val id:T)(val queries:Seq[String]) extends SqlMigrationInterface[T]

trait MigrationManager[T]{
  def migrations : Seq[Migration[T]]
  def ids = migrations.map(_.id)
  def alreadyAppliedIds : Seq[T]
  def notYetAppliedMigrations = migrations.drop(alreadyAppliedIds.size)
  def dblocation = System.getProperty("user.dir")+"/test.tb"
  def db = Database.forURL(s"jdbc:h2:$dblocation", driver = "org.h2.Driver")
  def up {
    // make sure there are no duplicate ids
    assert( Set(migrations.map(_.id):_*).size == migrations.size )
    db.withSession{
      while(notYetAppliedMigrations.size > 0){
        db.withTransaction{
          if(notYetAppliedMigrations.size > 0){
            assert( ids.take(alreadyAppliedIds.size) == alreadyAppliedIds )
            val migration = notYetAppliedMigrations.head
            try{
              beforeApply(migration)
              migration.up
              afterApply(migration)
            } catch {
              case e:Exception => threadLocalSession.rollback; throw e
            }
          }
        }
      }
    }
  }
  def beforeApply(migration:Migration[T]){}
  def afterApply(migration:Migration[T])
}
object MigrationsTable extends Table[Int]("__migrations__") {
  def id = column[Int]("id", O.PrimaryKey)
  def * = id
}

trait MyMigrationManager extends MigrationManager[Int]{
  def init = db.withSession(MigrationsTable.ddl.create)
  def alreadyAppliedIds = db.withSession{MigrationsTable.map(_.id).list}
  def latest = alreadyAppliedIds.last
  def afterApply(migration:Migration[Int]) = {
    db.withSession{MigrationsTable.insert( migration.id )}
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
    db.withSession{updateNA("DROP ALL OBJECTS DELETE FILES").execute}
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