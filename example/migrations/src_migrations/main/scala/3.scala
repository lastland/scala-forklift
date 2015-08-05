import scala.migrations.slick.H2Migration.SqlMigration

object M3 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration( 3 )(List(
    // SQL for changing the table again, as we do not have a type-safe, db independent API for that in Slick yet
    """alter table "users" alter column "first" rename to "firstname" """,
    """alter table "users" alter column "last" rename to "lastname" """
  ))
}
