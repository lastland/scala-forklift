package com.liyaos.forklift.slick.tests.unittests

import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.slick._
import org.scalatest._
import java.sql.SQLException
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

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
        SqlMigration(3)(List(
          sqlu"""alter table "users" alter column "first" rename to "firstname" """,
          sqlu"""alter table "users" alter column "last" rename to "lastname" """
        )))
  }

  "up" should "apply the migrations" in {
    val m = new SlickMigrationManager {
      override lazy val dbConfig = theDBConfig(0)
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
      override lazy val dbConfig = theDBConfig(1)
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
      override lazy val dbConfig = theDBConfig(2)
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
