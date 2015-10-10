package com.liyaos.forklift.slick.tests.unittests

import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.slick._
import org.scalatest._
import java.sql.SQLException
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import slick.jdbc.meta.MTable
import slick.driver.JdbcProfile

trait MigrationTests extends FlatSpec with PrivateMethodTester {
  this: ConfigFile with Tables =>

  import profile.api._

  protected def getTables = MTable.getTables(None, None, None, None)

  class MigrationSeq {
    lazy val empty: List[Migration[Int, DBIO[Unit]]] = List()
    lazy val example = empty
    lazy val first = List(example.head)
  }

  val MigrationSeq: MigrationSeq = new MigrationSeq {
    override lazy val example: List[Migration[Int, DBIO[Unit]]] =
      List(SqlMigration(1)(List(
        sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR(255) NOT NULL,"last" VARCHAR(255) NOT NULL)""")),
        DBIOMigration(2)(
          DBIO.seq(UsersV2 ++= Seq(
            UsersRow(1, "Chris","Vogt"),
            UsersRow(2, "Yao","Li")
          ))),
        // SQLite does not support renaming columns directly
        SqlMigration(3)(List(
          sqlu"""alter table "users" rename to "users_old" """,
          sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY, "firstname" VARCHAR(255) NOT NULL, "lastname" VARCHAR(255) NOT NULL)""",
          sqlu"""insert into "users"("id", "firstname", "lastname") select "id", "first", "last" from "users_old" """,
          sqlu"""drop table "users_old" """
        )))
  }

  "init" should "create the migration tables" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.empty
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        getTables), 1 second)
      assume(!tablesBefore.exists(_.name.name == "__migrations__"))
      assume(!tablesBefore.exists(_.name.name == "users"))
      m.init
      val tablesAfter = Await.result(m.db.run(
        getTables), 1 second).toList
      assert(tablesAfter.exists(_.name.name == "__migrations__"))
    } finally {
      m.reset
      m.db.close()
    }
  }

  "reset" should "drop the migration table" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.empty
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        getTables), 1 second)
      assume(!tablesBefore.exists(_.name.name == "__migrations__"))
      assume(!tablesBefore.exists(_.name.name == "users"))
      m.init
      m.reset
      val tablesReset = Await.result(m.db.run(
        getTables), 1 second).toList
      assert(!tablesBefore.exists(_.name.name == "__migrations__"))
      assert(!tablesBefore.exists(_.name.name == "users"))
    } finally {
      m.db.close()
    }
  }

  it should "drop all the tables" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.first
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        getTables), 1 second)
      assume(!tablesBefore.exists(_.name.name == "__migrations__"))
      assume(!tablesBefore.exists(_.name.name == "users"))
      m.init
      m.up
      m.reset
      val tablesReset = Await.result(m.db.run(
        getTables), 1 second).toList
      assert(!tablesBefore.exists(_.name.name == "__migrations__"))
      assert(!tablesBefore.exists(_.name.name == "users"))
    } finally {
      m.db.close()
    }
  }

  "up" should "apply the migrations" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.example
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        getTables), 1 second)
      assume(!tablesBefore.exists(_.name.name == "__migrations__"))
      assume(!tablesBefore.exists(_.name.name == "users"))
      m.init
      m.up
      val f = m.db.run {
        UsersV3.result
      } map { users =>
        for (user <- users) yield (user.id, user.first, user.last)
      }
      val us = Await.result(f, 1 second)
      assert(us.toSet === Set((1, "Chris", "Vogt"), (2, "Yao", "Li")))
    } finally {
      m.reset
      m.db.close()
    }
  }

  it should "deprecate object models of previous versions" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.example
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        getTables), 1 second)
      assume(!tablesBefore.exists(_.name.name == "__migrations__"))
      assume(!tablesBefore.exists(_.name.name == "users"))
      m.init
      m.up
      val f = m.db.run {
        UsersV2.result
      } map { users =>
        for (user <- users) yield (user.first, user.last)
      }
      intercept[SQLException] {
        Await.result(f, 1 second)
      }
    } finally {
      m.reset
      m.db.close()
    }
  }

  it should "apply empty migrations with no exception" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.empty
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        getTables), 1 second)
      assume(!tablesBefore.exists(_.name.name == "__migrations__"))
      assume(!tablesBefore.exists(_.name.name == "users"))
      m.init
      m.up
    } catch {
      case e: Throwable =>
        // should never reach here
        assert(e === null)
    } finally {
      m.reset
      m.db.close()
    }
  }
}

class H2MigrationTests extends MigrationTests with H2ConfigFile

class SQLiteMigrationTests extends MigrationTests with SQLiteConfigFile

class MySQLMigrationTests extends MigrationTests with MySQLConfigFile {
  import profile.api._

  override val MigrationSeq = new MigrationSeq {
    override lazy val example: List[Migration[Int, DBIO[Unit]]] =
      List(SqlMigration(1)(List(
        sqlu"""create table `users` (`id` INTEGER NOT NULL PRIMARY KEY,`first` VARCHAR(255) NOT NULL,`last` VARCHAR(255) NOT NULL)""")),
        DBIOMigration(2)(
          DBIO.seq(UsersV2 ++= Seq(
            UsersRow(1, "Chris","Vogt"),
            UsersRow(2, "Yao","Li")
          ))),
        SqlMigration(3)(List(
          sqlu"""alter table `users` change `first` `firstname` VARCHAR(255)""",
          sqlu"""alter table `users` change `last` `lastname` VARCHAR(255)"""
        )))
  }
}

class PostgresMigrationTests extends MigrationTests with PostgresConfigFile

class HsqldbMigrationTests extends MigrationTests with HsqldbConfigFile

class DerbyMigrationTests extends MigrationTests with DerbyConfigFile {
  import profile.api._

  override val MigrationSeq: MigrationSeq = new MigrationSeq {
    override lazy val example: List[Migration[Int, DBIO[Unit]]] =
      List(SqlMigration(1)(List(
        sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY, "first" VARCHAR(255) NOT NULL, "last" VARCHAR(255) NOT NULL)""")),
        DBIOMigration(2)(
          DBIO.seq(UsersV2 ++= Seq(
            UsersRow(1, "Chris","Vogt"),
            UsersRow(2, "Yao","Li")
          ))),
        // SQLite does not support renaming columns directly
        SqlMigration(3)(List(
          sqlu"""rename column "users"."first" to "firstname" """,
          sqlu"""rename column "users"."last" to "lastname" """
        )))
  }
}
