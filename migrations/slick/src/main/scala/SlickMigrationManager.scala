package scala.migrations.slick

import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession
import scala.migrations.MigrationManager

class MigrationsTable(tag: Tag) extends Table[Int](tag, "__migrations__") {
  def id = column[Int]("id", O.PrimaryKey)
  def * = id
}

trait SlickMigrationManager[T] extends MigrationManager[T] {
  def dblocation = System.getProperty("user.dir")+"/test.tb"
  def db = Database.forURL(s"jdbc:h2:$dblocation", driver = "org.h2.Driver")
  override def up {
    db.withDynSession {
      super.up
    }
  }

  override def singleUp {
    db.withDynTransaction {
      super.singleUp
    }
  }

  override def rollback {
    dynamicSession.rollback
  }
}
