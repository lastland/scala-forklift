package com.liyaos.forklift.slick.tests.unittests

import java.time.Instant
import java.util.HashMap
import com.typesafe.config._
import ammonite.ops._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait ConfigFile {
  this: Tables =>
  val path = System.getProperty("user.dir")
  val timeout = Integer.valueOf(5000)
  val driver: String
  val dbDriver: String
  val dbUrl: String

  protected def dbMap = {
    val dbMap = new HashMap[String, Object]
    dbMap.put("url", dbUrl)
    dbMap.put("driver", dbDriver)
    dbMap.put("connectionTimeout", timeout)
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
    val tmpDir = tmp.dir()
    val handled = tmpDir/Symbol("main")/Symbol("scala")
    mkdir! handled
    val migrationsMap = new HashMap[String, Object]
    migrationsMap.put("slick", slickMap)
    migrationsMap.put("unhandled_location",
      s"$path/example/migrations/src_migrations/main/scala")
    migrationsMap.put("handled_location", handled.toString)
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

  def theConfig = ConfigFactory.parseMap(content)

  def theDBConfig =
    DatabaseConfig.forConfig[JdbcProfile]("migrations.slick",
      theConfig)
}

trait H2ConfigFile extends ConfigFile with Tables {
  val driver = "slick.jdbc.H2Profile$"
  val dbDriver = "org.h2.Driver"
  val dbUrl = s"jdbc:h2:$path/test"

  val profile = slick.jdbc.H2Profile
}

trait SQLiteConfigFile extends ConfigFile with Tables {
  val driver = "slick.jdbc.SQLiteProfile$"
  val dbDriver = "org.sqlite.JDBC"
  val dbUrl = s"jdbc:sqlite:$path/target/test-${Instant.now.toEpochMilli}.sqlite.db"

  val profile = slick.jdbc.SQLiteProfile
}

trait MySQLConfigFile extends ConfigFile with Tables {
  val user = "root"
  val driver = "slick.jdbc.MySQLProfile$"
  val dbDriver = "com.mysql.cj.jdbc.Driver"
  val dbUrl = s"jdbc:mysql://localhost/circle_test?useSSL=false"

  val profile = slick.jdbc.MySQLProfile

  protected override def dbMap = {
    val dbMap = super.dbMap
    dbMap.put("user", user)
    dbMap
  }
}

trait PostgresConfigFile extends ConfigFile with Tables {
  val user = System.getProperty("user.name")
  val driver = "slick.jdbc.PostgresProfile$"
  val dbDriver = "org.postgresql.Driver"
  val dbUrl = s"jdbc:postgresql://localhost/circle_test"

  val profile = slick.jdbc.PostgresProfile

  protected override def dbMap = {
    val dbMap = super.dbMap
    dbMap.put("user", user)
    dbMap
  }
}

trait HsqldbConfigFile extends ConfigFile with Tables {
  val driver = "slick.jdbc.HsqldbProfile$"
  val dbDriver = "org.hsqldb.jdbc.JDBCDriver"
  val dbUrl = s"jdbc:hsqldb:mem:test"

  val profile = slick.jdbc.HsqldbProfile
}

trait DerbyConfigFile extends ConfigFile with Tables {
  val driver = "slick.jdbc.DerbyProfile$"
  val dbDriver = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbUrl = s"jdbc:derby:$path/target/test-${Instant.now.toEpochMilli}.derby.db;create=true"
  override val timeout = Integer.valueOf(10000)

  val profile = slick.jdbc.DerbyProfile
}
