package com.liyaos.forklift.slick

import com.liyaos.forklift.core.Migration
import slick.dbio.DBIO
import slick.migration.api.{SqlMigration => SqlMigrationAPI}

case class APIMigration[S](val id: S)(val migration: SqlMigrationAPI)
    extends Migration[S, DBIO[Unit]]{
  def up = migration.apply()
}
