import sbt._
import Keys._
import Tests._

object migrationBuild extends Build {
  val repoKind = SettingKey[String]("repo-kind",
    "Maven repository kind (\"snapshots\" or \"releases\")")

  lazy val commonSettings = Seq(
    organization := "com.liyaos",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.11.6",
    scalacOptions += "-deprecation",
    scalacOptions += "-feature",
    repoKind <<= (version)(v =>
      if(v.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"),
    publishTo <<= (repoKind){
      case "snapshots" => Some("snapshots" at
          "https://oss.sonatype.org/content/repositories/snapshots")
      case "releases" =>  Some("releases"  at
          "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"))

  lazy val coreProject = Project(
    "scala-forklift-core", file("core")).settings(commonSettings:_*)
  lazy val slickMigrationProject = Project(
    "scala-forklift-slick", file("migrations/slick")).dependsOn(
    coreProject).settings(commonSettings:_*)
  lazy val plainMigrationProject = Project(
    "scala-forklift-plain", file("migrations/plain")).dependsOn(
    coreProject).settings(commonSettings:_*)
  lazy val gitToolProject = Project(
    "scala-forklift-git-tools", file("tools/git")).dependsOn(
    coreProject).settings(commonSettings:_*)
}
