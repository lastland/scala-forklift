package com.liyaos.migrations.plain

import com.liyaos.migrations.core.Migration

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
