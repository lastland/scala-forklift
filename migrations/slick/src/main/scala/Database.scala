package scala.migrations.slick

import scala.slick.jdbc.JdbcBackend._
import scala.migrations.MigrationDatabase

class SlickMigrationDatabase(db: Any, objPath: String) extends MigrationDatabase {
  def copy(commitId: String) {
    db match {
      // TODO:
      // case h2db: scala.slick.driver.H2Driver.simple.Database => ()
      case _ =>
        throw new RuntimeException("Database not supported!")
    }
  }

  def use(mainBranchId: String) {
    db match {
      // TODO:
      // case h2db: scala.slick.driver.H2Driver.simple.Database => ()
      case _ =>
        throw new RuntimeException("Database not supported!")
    }
  }
}
