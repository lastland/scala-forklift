import scala.slick.migrations._

object SampleMigrations extends MyMigrationManager{
  import scala.slick.driver.H2Driver.simple._
  // WARNING!! never change version number or contents of any already published migration
  def migrations  = List(
    SqlMigration( 1 )(List(
       """create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)"""
    ))
    /*
    ,GenericMigration( 2 )({
      // this is typesafe :), but requires the corresponding code version to have been generated
      import datamodel.v1.schema.tables.Users
      import datamodel.v1.schema.entities.User
      // if you really have to do content changes in migrations, make sure they cannot conflict with data in one of your installations
      implicit session =>  Users.insertAll(
        User(1,"Chris","Vogt"),
        User(2,"Stefan","Zeiger")
      )
    })
    ,SqlMigration( 3 )(List(
      // SQL for changing the table again, as we do not have a type-safe, db independent API for that in Slick yet
       """alter table "users" alter column "first" rename to "firstname" """,
       """alter table "users" alter column "last" rename to "lastname" """
    ))
    */
  )
}
