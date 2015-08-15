package scala.migrations

trait Migration[T, S]{
  def id : T
  def up : S
}

trait MigrationManager[T, S] {
  var migrations : Seq[Migration[T, S]] = List()
  def ids = migrations.map(_.id)
  def alreadyAppliedIds : Seq[T]
  def notYetAppliedMigrations = migrations.filter(
    m => !alreadyAppliedIds.exists(_ == m.id))

  def init: Unit
  def up() {
    val ids = migrations.map(_.id)
    up(notYetAppliedMigrations.iterator)
  }

  protected def up(migrations: Iterator[Migration[T, S]]) {
    migrations foreach { m => m.up }
  }

  def reset: Unit
}
