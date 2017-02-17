package com.liyaos.forklift.slick.tests.unittests

import com.liyaos.forklift.core.Migration
import com.liyaos.forklift.slick._
import org.scalatest._
import java.io.ByteArrayOutputStream
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import slick.jdbc.meta.MTable

trait CommandTests extends FlatSpec {
  this: ConfigFile with Tables =>
  import profile.api._

  val waitTime = 5 seconds

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

  val PreviewSeq: Seq[String] = List(
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

--------------------------------------------------------------------------------
""",
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

1 SqlMigration:
${"\t"}[create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR(255) NOT NULL,"last" VARCHAR(255) NOT NULL)]

--------------------------------------------------------------------------------
""",
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

2 DBIOMigration:
${"\t"}CommandTests.profile.api.queryInsertActionExtensionMethods[CommandTests.this.UsersV2#TableElementType, Seq](CommandTests.UsersV2).++=(scala.collection.Seq[com.liyaos.forklift.slick.tests.unittests.UsersRow](UsersRow(1, "Chris", "Vogt"), UsersRow(2, "Yao", "Li")))

--------------------------------------------------------------------------------
""")

  "initOp" should "create migration tables" in {
    val m = new SlickMigrationManager
        with SlickMigrationCommands
        with SlickCodegen {
      override lazy val dbConfig = theDBConfig
      override lazy val config = theConfig
      migrations = MigrationSeq.empty
    }
    try {
      m.initOp
      val tablesAfter = Await.result(m.db.run(
        getTables), waitTime).toList
      assert(tablesAfter.exists(_.name.name == "__migrations__"))
    } finally {
      m.reset
      m.db.close()
    }
  }

  "previewOp" should "display the no migration when there's nothing to preview" in {
    val m = new SlickMigrationManager
        with SlickMigrationCommands
        with SlickCodegen {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.empty
    }
    try {
      m.init
      val stream = new ByteArrayOutputStream()
      Console.withOut(stream) {
        m.previewOp
      }
      val output = stream.toString
      assert(output === PreviewSeq(0))
    } finally {
      m.reset
      m.db.close()
    }
  }

  it should "display the sql migrations to be applied" in {
    val m = new SlickMigrationManager
        with SlickMigrationCommands
        with SlickCodegen {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.first
    }
    try {
      m.init
      val stream = new ByteArrayOutputStream()
      Console.withOut(stream) {
        m.previewOp
      }
      val output = stream.toString
      assert(output === PreviewSeq(1))
    } finally {
      m.reset
      m.db.close()
    }
  }

  it should "display the dbio migrations to be applied" in {
    val m = new SlickMigrationManager
        with SlickMigrationCommands
        with SlickCodegen {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.first
    }
    try {
      m.init
      m.up
      m.migrations = MigrationSeq.example.take(2)
      val stream = new ByteArrayOutputStream()
      Console.withOut(stream) {
        m.previewOp
      }
      val output = stream.toString
      assert(output === PreviewSeq(2))
    } finally {
      m.reset
      m.db.close()
    }
  }

  "applyOp" should "apply the migrations" in {
    val m = new SlickMigrationManager
        with SlickMigrationCommands
        with SlickCodegen {
      override lazy val dbConfig = theDBConfig
      migrations = MigrationSeq.example
    }
    try {
      m.init
      m.applyOp
      val f = m.db.run {
        UsersV3.result
      } map { users =>
        for (user <- users) yield (user.id, user.first, user.last)
      }
      val us = Await.result(f, waitTime)
      assert(us.toSet === Set((1, "Chris", "Vogt"), (2, "Yao", "Li")))
    } finally {
      m.reset
      m.db.close()
    }
  }

  "migrateOp" should "apply the migrations if there are migrations to apply" in {
    val m = new SlickMigrationManager
        with SlickMigrationCommands
        with SlickCodegen {
      override lazy val dbConfig = theDBConfig
      override lazy val config = theConfig
      migrations = MigrationSeq.example
    }
    try {
      m.init
      m.migrateOp(Seq())
      val f = m.db.run {
        UsersV3.result
      } map { users =>
        for (user <- users) yield (user.id, user.first, user.last)
      }
      val us = Await.result(f, waitTime)
      assert(us.toSet === Set((1, "Chris", "Vogt"), (2, "Yao", "Li")))
    } finally {
      m.reset
      m.db.close()
    }
  }
}

class H2CommandTests extends CommandTests with H2ConfigFile

class SQLiteCommandTests extends CommandTests with SQLiteConfigFile {
  import profile.api._

  class TestSlickMigrationManager extends SlickMigrationManager
      with SlickMigrationCommands with SlickCodegen {
    override lazy val dbConfig = theDBConfig
    override lazy val config = theConfig
    migrations = MigrationSeq.example
  }

  "migrateOp" should "apply the migrations if the db is deleted after successful migrations" in {
    var m = new TestSlickMigrationManager
    try {
      m.init
      m.migrateOp(Seq())
    } finally {
      m.db.close()
    }
    val dbFile = new java.io.File(dbUrl.substring("jdbc:sqlite:".length))
    dbFile.delete()
    m = new TestSlickMigrationManager
    try {
      m.init
      m.migrateOp(Seq())
      val f = m.db.run {
        UsersV3.result
      } map { users =>
        for (user <- users) yield (user.id, user.first, user.last)
      }
      val us = Await.result(f, waitTime)
      assert(us.toSet === Set((1, "Chris", "Vogt"), (2, "Yao", "Li")))
    } finally {
      m.reset
      m.db.close()
    }
  }
}

class MySQLCommandTests extends CommandTests with MySQLConfigFile {
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

  override val PreviewSeq: Seq[String] = List(
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

--------------------------------------------------------------------------------
""",
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

1 SqlMigration:
${"\t"}[create table `users` (`id` INTEGER NOT NULL PRIMARY KEY,`first` VARCHAR(255) NOT NULL,`last` VARCHAR(255) NOT NULL)]

--------------------------------------------------------------------------------
""",
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

2 DBIOMigration:
${"\t"}MySQLCommandTests.profile.api.queryInsertActionExtensionMethods[MySQLCommandTests.this.UsersV2#TableElementType, Seq](MySQLCommandTests.UsersV2).++=(collection.Seq[com.liyaos.forklift.slick.tests.unittests.UsersRow](UsersRow(1, "Chris", "Vogt"), UsersRow(2, "Yao", "Li")))

--------------------------------------------------------------------------------
""")
}

class PostgresCommandTests extends CommandTests with PostgresConfigFile

class DerbyCommandTests extends CommandTests with DerbyConfigFile {
  import profile.api._

  override val MigrationSeq: MigrationSeq = new MigrationSeq {
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
          sqlu"""rename column "users"."first" to "firstname" """,
          sqlu"""rename column "users"."last" to "lastname" """
        )))
  }

  override val PreviewSeq: Seq[String] = List(
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

--------------------------------------------------------------------------------
""",
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

1 SqlMigration:
${"\t"}[create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR(255) NOT NULL,"last" VARCHAR(255) NOT NULL)]

--------------------------------------------------------------------------------
""",
s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

2 DBIOMigration:
${"\t"}DerbyCommandTests.profile.api.queryInsertActionExtensionMethods[DerbyCommandTests.this.UsersV2#TableElementType, Seq](DerbyCommandTests.UsersV2).++=(collection.Seq[com.liyaos.forklift.slick.tests.unittests.UsersRow](UsersRow(1, "Chris", "Vogt"), UsersRow(2, "Yao", "Li")))

--------------------------------------------------------------------------------
""")
}
