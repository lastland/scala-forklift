package example.migration.manager

import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.slick.SlickMigrationManager
//import scala.migrations.plain.PlainMigrationManager

trait MyMigrationManager extends SlickMigrationManager {
//  override def beforeApply(migration:Migration[Int]) = {
//    println("applying migration "+migration.id)
//  }
}
