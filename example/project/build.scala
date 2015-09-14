import sbt._
import Keys._

object AppBuild extends Build {
  lazy val commonSettings = Seq(
    organization := "com.liyaos",
    version := "1.0",
    scalaVersion := "2.11.6",
    scalacOptions += "-deprecation",
    scalacOptions += "-feature")

  lazy val example = Project("example", file(".")).aggregate(
    app, migrations, migrationManager, generatedCode, tools).settings(
    commonSettings:_*)

  lazy val app = Project("app",
    file("app")).dependsOn(generatedCode).settings(commonSettings:_*)

  lazy val migrationManager = Project("migration_manager",
    file("migration_manager")).settings(commonSettings:_*)

  lazy val migrations = Project("migrations",
    file("migrations")).dependsOn(generatedCode, migrationManager).settings(
    commonSettings:_*)

  lazy val tools = Project("git-tools",
    file("tools/git")).settings(commonSettings:_*)

  lazy val generatedCode = Project("generate_code",
    file("generated_code")).settings(commonSettings:_*)
}
