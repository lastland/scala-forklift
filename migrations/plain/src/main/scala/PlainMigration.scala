package scala.migrations.plain

import scala.migrations.Migration

trait PlainMigrationInterface[T] extends Migration[T, Unit] {
  def queries: () => Unit
  override def up {
    queries()
  }
}

case class PlainMigration[T](val id: T)(q: => Unit)
    extends PlainMigrationInterface[T] {
  override def queries = () => q
}
