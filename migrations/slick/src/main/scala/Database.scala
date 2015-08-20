package com.liyaos.forklift.slick

import slick.jdbc.JdbcBackend._
import com.liyaos.forklift.core.MigrationDatabase

class SlickMigrationDatabase(db: Any, objPath: String) extends MigrationDatabase {
  def copy(branch: String, commitId: String) {
    db match {
      // TODO:
      // case h2db: scala.slick.driver.H2Driver.simple.Database => ()
      case _ =>
        throw new RuntimeException("Database not supported!")
    }
  }

  def use(branch: String, commitId: String) {
    db match {
      // TODO:
      // case h2db: scala.slick.driver.H2Driver.simple.Database => ()
      case _ =>
        throw new RuntimeException("Database not supported!")
    }
  }

  def rebuild(branch: String, commitId: String) {
    db match {
      // TODO:
      // case h2db: scala.slick.driver.H2Driver.simple.Database => ()
      case _ =>
        throw new RuntimeException("Database not supported!")
    }
  }
}
