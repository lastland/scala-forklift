organization := "com.typesafe"

name := "migrations-example"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

addCommandAlias("mg", "migrations/run")
