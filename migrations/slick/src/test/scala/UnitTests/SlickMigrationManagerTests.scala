package com.liyaos.forklift.slick.tests.unittests

import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.slick._
import org.scalatest._
import java.sql.SQLException
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.meta.MTable

trait MigrationTests extends FlatSpec with PrivateMethodTester {
  this: ConfigFile with Tables =>

  import profile.api._

  object MigrationSeq {
    lazy val empty: List[Migration[Int, DBIO[Unit]]] = List()

    lazy val example: List[Migration[Int, DBIO[Unit]]] =
      List(SqlMigration(1)(List(
        sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)""")),
        DBIOMigration(2)(
          DBIO.seq(UsersV2 ++= Seq(
            UsersRow(1, "Chris","Vogt"),
            UsersRow(2, "Yao","Li")
          ))),
        // SQLite does not support renaming columns directly
        SqlMigration(3)(List(
          sqlu"""alter table "users" rename to "users_old" """,
          sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY, "firstname" VARCHAR NOT NULL, "lastname" VARCHAR NOT NULL)""",
          sqlu"""insert into "users"("id", "firstname", "lastname") select "id", "first", "last" from "users_old" """,
          sqlu"""drop table "users_old" """
        )))
  }

  object NextInt {
    var counter = 0
    def next = synchronized {
      counter += 1
      counter
    }
  }

  "init" should "create the migration tables" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(NextInt.next)
      migrations = MigrationSeq.empty
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        MTable.getTables), Duration.Inf).toList
      assert(tablesBefore.length === 0)
      m.init
      val tablesAfter = Await.result(m.db.run(
        MTable.getTables), Duration.Inf).toList
      assert(tablesAfter.exists(_.name.name == "__migrations__"))
    } finally {
      m.reset
      m.db.close()
    }
  }

  "reset" should "drop the migration table" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(NextInt.next)
      migrations = MigrationSeq.empty
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        MTable.getTables), Duration.Inf).toList
      assert(tablesBefore.length === 0)
      m.init
      m.reset
      val tablesReset = Await.result(m.db.run(
        MTable.getTables), Duration.Inf).toList
      assert(tablesReset.length === 0)
    } finally {
      m.db.close()
    }
  }

  it should "drop all the tables" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(NextInt.next)
      migrations = MigrationSeq.example
    }
    try {
      val tablesBefore = Await.result(m.db.run(
        MTable.getTables), Duration.Inf).toList
      assert(tablesBefore.length === 0)
      m.init
      m.up
      m.reset
      val tablesReset = Await.result(m.db.run(
        MTable.getTables), Duration.Inf).toList
      assert(tablesReset.length === 0)
    } finally {
      m.db.close()
    }
  }

  "up" should "apply the migrations" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(NextInt.next)
      migrations = MigrationSeq.example
    }
    try {
      m.init
      m.up
      val f = m.db.run {
        UsersV3.result
      } map { users =>
        for (user <- users) yield (user.first, user.last)
      }
      val us = Await.result(f, Duration.Inf)
      assert(us.toSet === Set(("Chris", "Vogt"), ("Yao", "Li")))
    } finally {
      m.reset
      m.db.close()
    }
  }

  it should "deprecate object models of previous versions" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(NextInt.next)
      migrations = MigrationSeq.example
    }
    try {
      m.init
      m.up
      val f = m.db.run {
        UsersV2.result
      } map { users =>
        for (user <- users) yield (user.first, user.last)
      }
      intercept[SQLException] {
        Await.result(f, Duration.Inf)
      }
    } finally {
      m.reset
      m.db.close()
    }
  }

  it should "apply empty migrations with no exception" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(NextInt.next)
      migrations = MigrationSeq.empty
    }
    try {
      m.init
      m.up
    } catch {
      case e =>
        // should not be here
        assert(e === null)
    } finally {
      m.reset
      m.db.close()
    }
  }
}

class H2MigrationTests extends MigrationTests with H2ConfigFile

class SQLiteMigrationTests extends MigrationTests with SQLiteConfigFile
