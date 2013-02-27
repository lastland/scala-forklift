package scala.slick.migrations

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
class SqlMigration[T](val id:T)(val queries:Seq[String]) extends SqlMigrationInterface[T]
class GenericMigration[T]( val id:T )(f : Session => Unit) extends Migration[T]{
  def up : Unit = f(threadLocalSession)
}
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
  def reset = {
    db.withSession{updateNA("DROP ALL OBJECTS DELETE FILES").execute}
    init
  }
  def alreadyAppliedIds = db.withSession{MigrationsTable.map(_.id).list}
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
}