package scala.migrations.slick

import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery._
import scala.migrations.Migration

trait SqlMigrationInterface[T] extends Migration[T]{
  def queries : Seq[String]
  def up{
    queries.foreach(updateNA(_).execute)
  }
}
case class SqlMigration[T] (val id:T)(val queries:Seq[String])
    extends SqlMigrationInterface[T]
