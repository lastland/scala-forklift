package com.liyaos.forklift.slick.tests.unittests

import ammonite.ops._
import com.typesafe.config._
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait ConfigFile {
  this: Tables =>
  val path: Path
  val driver: String
  val dbDriver: String
  def dbUrl(n: Int): String

  def content(n: Int) = {
    import java.util.HashMap
    val dbMap = new HashMap[String, Object]
    dbMap.put("url", dbUrl(n))
    dbMap.put("driver", dbDriver)
    val slickMap = new HashMap[String, Object]
    slickMap.put("db", dbMap)
    slickMap.put("driver", driver)
    slickMap.put("version_control_dir", s"$path/.db")
    val migrationsMap = new HashMap[String, Object]
    migrationsMap.put("slick", slickMap)
    val finalMap = new HashMap[String, Object]
    finalMap.put("migrations", migrationsMap)
    finalMap
  }

  def theDBConfig(n: Int) =
    DatabaseConfig.forConfig[JdbcProfile]("migrations.slick",
      ConfigFactory.parseMap(content(n)))
}

trait H2ConfigFile extends ConfigFile with Tables {
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.H2Driver$"
  val dbDriver = "org.h2.Driver"
  def dbUrl(n: Int) = s"jdbc:h2:$path/test$n"

  val profile = slick.driver.H2Driver
}

trait SQLiteConfigFile extends ConfigFile with Tables {
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.SQLiteDriver$"
  val dbDriver = "org.sqlite.JDBC"
  def dbUrl(n: Int) = s"jdbc:sqlite:$path/test$n.sqlite.db"

  val profile = slick.driver.SQLiteDriver
}

trait MySQLConfigFile extends ConfigFile with Tables {
  val user = System.getProperty("user.name")
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.MySQLDriver$"
  val dbDriver = "com.mysql.jdbc.Driver"
  def dbUrl(n: Int) = s"jdbc:mysql://$user@localhost/test$n"

  val profile = slick.driver.MySQLDriver
}
