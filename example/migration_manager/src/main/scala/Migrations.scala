package example.migration.manager

import com.liyaos.migrations.core.Migration
import com.liyaos.migrations.slick.SlickMigrationManager
//import scala.migrations.plain.PlainMigrationManager

trait MyMigrationManager extends SlickMigrationManager {
//  override def beforeApply(migration:Migration[Int]) = {
//    println("applying migration "+migration.id)
//  }
}
