package scala.migrations.plain

import java.io._
import scala.io.Source
import scala.migrations.Migration
import scala.migrations.MigrationManager

trait PlainMigrationManager extends MigrationManager[Int] {
  def dblocation = System.getProperty("user.dir")+"/migrationInfo.txt"
  override def alreadyAppliedIds = 1 to latest
  override def init = {
    val writer = new PrintWriter(new File(dblocation))
    writer.write("0")
    writer.close()
  }
  override def latest = {
    val source = Source.fromFile(dblocation)
    val res = source.getLines.next.toInt
    source.close
    res
  }
  override def afterApply(migration: Migration[Int]) = {
    val writer = new PrintWriter(new File(dblocation))
    writer.write( migration.id.toString )
    writer.close()
  }
  override def rollback {
    // intentionally do nothing
  }
  override def reset {
    init
  }
}
