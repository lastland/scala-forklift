# Scala-Forklift

[![Circle CI](https://circleci.com/gh/lastland/scala-forklift.svg?style=shield)](https://circleci.com/gh/lastland/scala-forklift)
[![Join the chat at https://gitter.im/lastland/scala-forklift](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/lastland/scala-forklift?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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

    libraryDependencies += "com.liyaos" %% "scala-forklift-slick" % "0.2.1"

check [example](/example) for tutorial and example code.

### Quick Start

Alternatively, you can use our start template on GitHub to quickly start a project with Scala-Forklift:

    git clone https://github.com/lastland/scala-forklift-start-template.git

## Known Issues

- The `reset` command may not correctly handle database schemas with foreign keys.
