import slick.migration.api.TableMigration
import slick.migration.api.H2Dialect
import com.liyaos.forklift.slick.APIMigration
import datamodel.v2.schema.tables._

object M3 {
  implicit val dialect = new H2Dialect

  MyMigrations.migrations = MyMigrations.migrations :+ APIMigration( 3 )(
    TableMigration(Users).
      renameColumn(_.first, "firstname").
      renameColumn(_.last, "lastname"))
}
