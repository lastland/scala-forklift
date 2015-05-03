import scala.migrations.Migration
import scala.migrations.slick.SlickMigrationManager

trait MyMigrationManager extends SlickMigrationManager {
  override def beforeApply(migration:Migration[Int]) = {
    println("applying migration "+migration.id)
  }

}
