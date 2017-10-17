package com.liyaos.forklift.slick

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait SlickMigrationsConfig {
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("migrations.slick")
}

object SlickMigrationsConfig extends SlickMigrationsConfig
