import sbt._
import Keys._
import Tests._

object migrationBuild extends Build {
  val repoKind = SettingKey[String]("repo-kind",
    "Maven repository kind (\"snapshots\" or \"releases\")")

  lazy val commonSettings = Seq(
    organization := "com.liyaos",
    licenses := Seq("BSD-2-Clause" -> url("http://opensource.org/licenses/BSD-2-Clause")),
    homepage := Some(url("https://github.com/lastland/scala-forklift")),
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.6",
    scalacOptions += "-deprecation",
    scalacOptions += "-feature",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    repoKind <<= (version)(v =>
      if(v.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"),
    publishTo <<= (repoKind){
      case "snapshots" => Some("snapshots" at
          "https://oss.sonatype.org/content/repositories/snapshots")
      case "releases" =>  Some("releases"  at
          "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    pomExtra := (
      <scm>
        <url>git@github.com:lastland/scala-forklift.git</url>
        <connection>scm:git:git@github.com:lastland/scala-forklift.git</connection>
      </scm>
      <developers>
        <developer>
        <id>lastland</id>
        <name>Yao Li</name>
        </developer>
      </developers>))

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
