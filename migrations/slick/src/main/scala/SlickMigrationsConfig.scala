package com.liyaos.migrations.slick

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

object SlickMigrationsConfig {
  val config = DatabaseConfig.forConfig[JdbcProfile]("migrations")
}
