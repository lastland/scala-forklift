# Scala-Forklift

Scala-Forklift helps manage and apply database migrations for your Scala project.

Write your migrations in plain SQL:

    MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(1)(List(
      sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)"""
    ))

Or type-safe Slick queries:

    MyMigrations.migrations = MyMigrations.migrations :+ DBIOMigration(2)(
      DBIO.seq(Users ++= Seq(
        UsersRow(1, "Chris","Vogt"),
        UsersRow(2, "Yao","Li")
      )))

Don't worry about the Scala code for your database schema. Our source code generator will have it generated for you automatically.

**Key Features**:

- Supports for Slick (and Casbah is on the way!).
- A source code generator to generate and manage Scala models from your database schemas.
- A tool to help you manage your dev db with git, with supports for branching and merging.
- High customizability.

## How to Use

Add the following dependency to your `build.sbt`:

    resolvers += Resolver.sonatypeRepo("snapshots")

    libraryDependencies += "com.liyaos" %% "scala-forklift-slick" % "0.1.0-SNAPSHOT"

check [example](/example) for tutorial and example code.

## Known Issues

- The `reset` command may not correctly handle database schemas with foreign keys.
