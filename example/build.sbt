organization := "com.typesafe"

name := "migrations-example"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

addCommandAlias("mgm", "migration_manager/run")

addCommandAlias("mg", "migrations/run")
