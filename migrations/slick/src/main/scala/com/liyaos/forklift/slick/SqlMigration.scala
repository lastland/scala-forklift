package com.liyaos.forklift.slick

import com.liyaos.forklift.core.Migration
import slick.dbio.DBIO

trait SqlMigrationInterface[T] extends Migration[T, DBIO[Unit]]{
  def queries : Seq[DBIO[Int]]
}

case class SqlMigration[T](val id:T)(val queries: Seq[DBIO[Int]])
    extends SqlMigrationInterface[T] {
  def up = DBIO.seq(queries:_*)
}
