package scala.slick.migrations

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

trait Migration[T]{
  def id : T
  def up : Unit
}
abstract class MigrationBase[T]( val id : T ) extends Migration[T]
class GenericMigration[T](id:T)(f : Session => Unit) extends MigrationBase(id){
  def up : Unit = f(threadLocalSession)
}
trait MigrationManager[T]{
  def migrations : Seq[Migration[T]]
  def ids = migrations.map(_.id)
  def alreadyAppliedIds : Seq[T]
  def notYetAppliedMigrations = migrations.drop(alreadyAppliedIds.size)
  
  val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  def up : Unit = {
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

