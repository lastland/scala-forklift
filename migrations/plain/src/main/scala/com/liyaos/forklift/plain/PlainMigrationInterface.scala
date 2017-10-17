package com.liyaos.forklift.plain

import com.liyaos.forklift.core.Migration

trait PlainMigrationInterface[T] extends Migration[T, Unit] {
  def queries: () => Unit
  override def up {
    queries()
  }
}
