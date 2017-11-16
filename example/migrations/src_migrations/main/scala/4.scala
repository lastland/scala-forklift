import slick.jdbc.H2Profile.api._
import com.liyaos.forklift.slick.SqlResourceMigration

object M4 {
  MyMigrations.migrations = MyMigrations.migrations :+ SqlResourceMigration(4, slick.jdbc.H2Profile, MyMigrations.class)
}