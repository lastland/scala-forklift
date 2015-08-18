organization := "com.typesafe"

name := "migrations-example-migrations"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= List(
  "com.liyaos" %% "slick-migrations" % "1.0-SNAPSHOT",
  "com.zaxxer" % "HikariCP" % "2.3.9"
)
