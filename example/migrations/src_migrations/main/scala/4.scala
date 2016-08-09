import slick.migration.api.TableMigration
import slick.migration.api.H2Dialect
import com.liyaos.forklift.slick.APIMigration
import datamodel.v3.schema.tables._

object M4 {
  implicit val dialect = new H2Dialect

  MyMigrations.migrations = MyMigrations.migrations :+ APIMigration( 4 )(
    TableMigration(Users).renameColumn(_.last, "lastname"))
}
