import scala.migrations.slick.H2Migration.SqlMigration

object M1 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(1)(List(
    """create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)"""
))
}
