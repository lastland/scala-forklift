import slick.driver.H2Driver.api._
import com.liyaos.forklift.slick.SqlMigration

object M3 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration( 3 )(List(
    // SQL for changing the table again, as we do not have a type-safe, db independent API for that in Slick yet
    sqlu"""alter table "users" alter column "first" rename to "firstname" """,
    sqlu"""alter table "users" alter column "last" rename to "lastname" """
  ))
}
