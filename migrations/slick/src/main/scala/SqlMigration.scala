package scala.migrations.slick

import scala.slick.jdbc.StaticQuery._
import scala.migrations.Migration

trait SqlMigrationInterface[T] extends Migration[T]{
  def queries : Seq[String]
}

object AccessMigration {
  case class SqlMigration[T](val id:T)(val queries:Seq[String])
      extends SqlMigrationInterface[T] {
    import scala.slick.driver.AccessDriver.simple._
    import Database.dynamicSession
    def up {
      queries.foreach(updateNA(_).execute)
    }
  }
}

object DerbyMigration {
  case class SqlMigration[T](val id:T)(val queries:Seq[String])
      extends SqlMigrationInterface[T] {
    import scala.slick.driver.DerbyDriver.simple._
    import Database.dynamicSession
    def up {
      queries.foreach(updateNA(_).execute)
    }
  }
}

object HsqldbMigration {
  case class SqlMigration[T](val id:T)(val queries:Seq[String])
      extends SqlMigrationInterface[T] {
    import scala.slick.driver.HsqldbDriver.simple._
    import Database.dynamicSession
    def up {
      queries.foreach(updateNA(_).execute)
    }
  }
}

object MySQLMigration {
  case class SqlMigration[T](val id:T)(val queries:Seq[String])
      extends SqlMigrationInterface[T] {
    import scala.slick.driver.MySQLDriver.simple._
    import Database.dynamicSession
    def up {
      queries.foreach(updateNA(_).execute)
    }
  }
}

object PostgresMigration {
  case class SqlMigration[T](val id:T)(val queries:Seq[String])
      extends SqlMigrationInterface[T] {
    import scala.slick.driver.PostgresDriver.simple._
    import Database.dynamicSession
    def up {
      queries.foreach(updateNA(_).execute)
    }
  }
}

object H2Migration {
  case class SqlMigration[T](val id:T)(val queries:Seq[String])
      extends SqlMigrationInterface[T] {
    import scala.slick.driver.H2Driver.simple._
    import Database.dynamicSession
    def up {
      queries.foreach(updateNA(_).execute)
    }
  }
}
