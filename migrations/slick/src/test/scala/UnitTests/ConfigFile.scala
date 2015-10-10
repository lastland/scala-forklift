package com.liyaos.forklift.slick.tests.unittests

import java.util.HashMap
import ammonite.ops._
import com.typesafe.config._
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait ConfigFile {
  this: Tables =>
  val path: Path
  val driver: String
  val dbDriver: String
  val dbUrl: String

  protected def dbMap = {
    val dbMap = new HashMap[String, Object]
    dbMap.put("url", dbUrl)
    dbMap.put("driver", dbDriver)
    dbMap
  }

  protected def slickMap = {
    val slickMap = new HashMap[String, Object]
    slickMap.put("db", dbMap)
    slickMap.put("driver", driver)
    slickMap.put("version_control_dir", s"$path/.db")
    slickMap
  }

  protected def migrationsMap = {
    val migrationsMap = new HashMap[String, Object]
    migrationsMap.put("slick", slickMap)
    migrationsMap
  }

  protected def finalMap = {
    val finalMap = new HashMap[String, Object]
    finalMap.put("migrations", migrationsMap)
    finalMap
  }

  def content = {
    finalMap
  }

  def theDBConfig =
    DatabaseConfig.forConfig[JdbcProfile]("migrations.slick",
      ConfigFactory.parseMap(content))
}

trait H2ConfigFile extends ConfigFile with Tables {
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.H2Driver$"
  val dbDriver = "org.h2.Driver"
  val dbUrl = s"jdbc:h2:$path/test"

  val profile = slick.driver.H2Driver
}

trait SQLiteConfigFile extends ConfigFile with Tables {
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.SQLiteDriver$"
  val dbDriver = "org.sqlite.JDBC"
  val dbUrl = s"jdbc:sqlite:$path/test.sqlite.db"

  val profile = slick.driver.SQLiteDriver
}

trait MySQLConfigFile extends ConfigFile with Tables {
  val user = System.getProperty("user.name")
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.MySQLDriver$"
  val dbDriver = "com.mysql.jdbc.Driver"
  val dbUrl = s"jdbc:mysql://localhost/circle_test"

  val profile = slick.driver.MySQLDriver

  protected override def dbMap = {
    val dbMap = super.dbMap
    dbMap.put("user", user)
    dbMap
  }
}

trait PostgresConfigFile extends ConfigFile with Tables {
  val user = System.getProperty("user.name")
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.PostgresDriver$"
  val dbDriver = "org.postgresql.Driver"
  val dbUrl = s"jdbc:postgresql://localhost/circle_test"

  val profile = slick.driver.PostgresDriver

  protected override def dbMap = {
    val dbMap = super.dbMap
    dbMap.put("user", user)
    dbMap
  }
}

trait HsqldbConfigFile extends ConfigFile with Tables {
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.HsqldbDriver$"
  val dbDriver = "org.hsqldb.jdbc.JDBCDriver"
  val dbUrl = s"jdbc:hsqldb:mem:test"

  val profile = slick.driver.HsqldbDriver
}

trait DerbyConfigFile extends ConfigFile with Tables {
  val path = Path(System.getProperty("user.dir"))
  val driver = "slick.driver.DerbyDriver$"
  val dbDriver = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbUrl = s"jdbc:derby:$path/test.derby.db;create=true"

  val profile = slick.driver.DerbyDriver
}
