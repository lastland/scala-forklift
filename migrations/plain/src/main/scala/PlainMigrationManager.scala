package com.liyaos.forklift.plain

import java.io._
import scala.io.Source
import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.core.MigrationManager

trait PlainMigrationManager extends MigrationManager[Int, Unit] {
  def dblocation = System.getProperty("user.dir")+"/migrationInfo.txt"
  override def alreadyAppliedIds = alreadyAppliedIds_
  override def init = {
    val writer = new PrintWriter(new File(dblocation))
    writer.write("0")
    writer.close()
  }

  private def alreadyAppliedIds_ = {
    val source = Source.fromFile(dblocation)
    source.getLines.map(_.toInt).toList
  }

  private def afterApply(migration: Migration[Int, Unit]) = {
    val writer = new PrintWriter(new FileOutputStream(
      new File(dblocation), true))
    writer.write( migration.id.toString )
    writer.close()
  }

  override def up(migrations: Iterator[Migration[Int, Unit]]) {
    migrations foreach { m =>
      m.up
      afterApply(m)
    }
  }

  override def reset {
    init
  }
}
