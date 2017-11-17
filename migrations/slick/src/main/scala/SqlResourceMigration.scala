package com.liyaos.forklift.slick

import com.liyaos.forklift.core.Migration
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.io.Source

trait SqlResourceMigrationInterface[T] extends Migration[T, DBIO[Unit]] {
  def sqlQueries: String
}

case class SqlResourceMigration[T](id: T, profile: JdbcProfile) extends SqlResourceMigrationInterface[T] {

  import profile.api._

  val sqlQueries = Source.fromResource(s"$id.sql").mkString

  def up = {
    slick.dbio.DBIO.seq(List(sqlu"#$sqlQueries"):_*)
  }
}

