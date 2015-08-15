import slick.driver.H2Driver.api._
import scala.migrations.slick.SqlMigration

object M1 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(1)(List(
    sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)"""
))
}
