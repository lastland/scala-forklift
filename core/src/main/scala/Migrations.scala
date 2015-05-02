package scala.migrations

trait Migration[T]{
  def id : T
  def up : Unit
}

trait MigrationManager[T]{
  def migrations : Seq[Migration[T]]
  def ids = migrations.map(_.id)
  def alreadyAppliedIds : Seq[T]
  def notYetAppliedMigrations = migrations.drop(alreadyAppliedIds.size)

  def beforeApply(migration:Migration[T]){}
  def afterApply(migration:Migration[T])
  def up {
    while(notYetAppliedMigrations.size > 0){
      singleUp
    }
  }

  def singleUp {
    if(notYetAppliedMigrations.size > 0){
      assert( ids.take(alreadyAppliedIds.size) == alreadyAppliedIds )
      val migration = notYetAppliedMigrations.head
      try{
        beforeApply(migration)
        migration.up
        afterApply(migration)
      } catch {
        case e:Exception => rollback; throw e
      }
    }
  }

  def rollback: Unit
}
