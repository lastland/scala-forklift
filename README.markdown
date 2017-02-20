# Scala-Forklift

[![Circle CI](https://circleci.com/gh/lastland/scala-forklift.svg?style=shield)](https://circleci.com/gh/lastland/scala-forklift)
[![Join the chat at https://gitter.im/lastland/scala-forklift](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/lastland/scala-forklift?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Scala-Forklift helps manage and apply database migrations for your Scala project.

Write your migrations in plain SQL:

```scala
MyMigrations.migrations = MyMigrations.migrations :+ SqlMigration(1)(List(
  sqlu"""create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)"""
))
```

Or type-safe Slick queries:

``` scala
MyMigrations.migrations = MyMigrations.migrations :+ DBIOMigration(2)(
  DBIO.seq(Users ++= Seq(
    UsersRow(1, "Chris","Vogt"),
    UsersRow(2, "Yao","Li")
  )))
```

Or use [slick-migration-api](https://github.com/nafg/slick-migration-api):

``` scala
MyMigrations.migrations = MyMigrations.migrations :+ APIMigration(3)(
  TableMigration(Users).
    renameColumn(_.first, "firstname").
    renameColumn(_.last, "lastname"))
```

(Note: `APIMigration` is not supported in versions prior to `v0.2.3`)

Don't worry about keeping the Scala code and your database schema consistent. Our source code generator will have it generated for you.

**Key Features**:

- Supports for type-safe database migration with [Slick](https://github.com/slick/slick) and [slick-migration-api](https://github.com/nafg/slick-migration-api).
- A source code generator to generate and manage Scala models from your database schemas.
- A tool to help you manage your dev db with git, with supports for branching and merging.
- High customizability.


## How to Use

Scala-Forklift supports both Slick 3.1 and Slick 3.2. The latest versions of Scala-Forklift are given below:

| Scala Version  | Slick Version | SBT dependency |
|----------------|---------------|----------------|
| 2.11.x         | `3.1.x`       | `libraryDependencies += "com.liyaos" %% "scala-forklift-slick" % "0.2.3"` |
| 2.11.x, 2.12.x | `3.2.x`       | `libraryDependencies += "com.liyaos" %% "scala-forklift-slick" % "0.3.0"` |

For tutorial and example code, please check [example](/example).

Here is also a wonderful [tutorial](http://blog.novatec-gmbh.de/database-migration-slick-scala-forklift/) written by Andreas Burkard and Julian Tragé.

### Quick Start

You can use our start template on GitHub to quickly start a project with Scala-Forklift:

``` shell
git clone https://github.com/lastland/scala-forklift-start-template.git
```

## More Examples

- [Demo with Play and Slick](https://github.com/lastland/play-slick-forklift-example)
- [Demo with Quill](https://github.com/lastland/scala-forklift-quill)

## Known Issues

- The `reset` command may not correctly handle database schemas with foreign keys.
