package com.liyaos.forklift.slick

import com.liyaos.forklift.core.Migration
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.io.Source

trait SqlResourceMigrationInterface[T] extends Migration[T, DBIO[Unit]] {
  def sqlResource: String
}

case class SqlResourceMigration[T](id: T, profile: JdbcProfile, clazz: Class[_]) extends SqlResourceMigrationInterface[T] {

  import profile.api._

  val sqlResource = Source.fromInputStream(clazz.getResourceAsStream(s"$id.sql")).mkString

  def up = {
    slick.dbio.DBIO.seq(List(sqlu"#$sqlResource"):_*)
  }
}

