package com.liyaos.forklift.plain

case class PlainMigration[T](val id: T)(q: => Unit)
    extends PlainMigrationInterface[T] {
  override def queries = () => q
}
