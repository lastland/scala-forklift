organization := "com.typesafe"

name := "migrations-example-generated-code"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0"
)
