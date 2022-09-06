val repoKind = SettingKey[String]("repo-kind",
  "Maven repository kind (\"snapshots\" or \"releases\")")

lazy val slickVersion = "3.3.3"

lazy val scala212 = "2.12.11"
lazy val scala213 = "2.13.8"
lazy val supportedScalaVersions = List(scala212, scala213)

lazy val coreDependencies = libraryDependencies ++= List(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "com.typesafe" % "config" % "1.4.2",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
)

lazy val slickDependencies = List(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "io.github.nafg" %% "slick-migration-api" % "0.8.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1"
)

lazy val slickDependenciesWithTests = slickDependencies ++ List(
  "org.scalatest" %% "scalatest" % "3.2.12",
  "com.lihaoyi" %% "ammonite-ops" % "2.4.1",
  "commons-io" % "commons-io" % "2.6",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.h2database" % "h2" % "2.1.214",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2",// 3.30.1 crashes SQLiteCommandTests
  "mysql" % "mysql-connector-java" % "8.0.30",
  "org.postgresql" % "postgresql" % "42.5.0",
  "org.hsqldb" % "hsqldb" % "2.7.0",
  "org.apache.derby" % "derby" % "10.15.2.0",
  "ch.qos.logback" % "logback-classic" % "1.2.11"
).map(_ % "test")

lazy val commonSettings = Seq(
  organization := "com.liyaos",
  licenses := Seq("Apache 2.0" ->
    url("https://github.com/lastland/scala-forklift/blob/master/LICENSE")),
  homepage := Some(url("https://github.com/lastland/scala-forklift")),
  scalaVersion := scala213,
  scalacOptions += "-deprecation",
  scalacOptions += "-feature",
  resolvers += Resolver.jcenterRepo,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  repoKind := { if (version.value.trim.endsWith("SNAPSHOT")) "snapshots"
                else "releases" },
  publishTo := { repoKind.value match {
    case "snapshots" => Some("snapshots" at
        "https://oss.sonatype.org/content/repositories/snapshots")
    case "releases" =>  Some("releases"  at
        "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  }},
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

// Derby is running is secured mode since version 10.12.1.1, so security manager must be disabled for tests  
// https://stackoverflow.com/questions/48008343/sbt-test-does-not-work-for-spark-test
// https://issues.apache.org/jira/browse/DERBY-6648
Test / testOptions += Tests.Setup(() => System.setSecurityManager(null))

lazy val root = Project(
  "scala-forklift", file(".")).settings(
  crossScalaVersions := Nil,
  publishArtifact := false).aggregate(
  coreProject, slickMigrationProject, plainMigrationProject, gitToolProject)

lazy val coreProject = Project(
  "scala-forklift-core", file("core")).settings(
  commonSettings:_*).settings {Seq(
    crossScalaVersions := supportedScalaVersions,
    coreDependencies
  )}

lazy val slickMigrationProject = Project(
  "scala-forklift-slick", file("migrations/slick")).dependsOn(
  coreProject).settings(commonSettings:_*).settings { Seq(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= slickDependenciesWithTests
  )} 

lazy val plainMigrationProject = Project(
  "scala-forklift-plain", file("migrations/plain")).dependsOn(
  coreProject).settings(commonSettings:_*).settings(crossScalaVersions := supportedScalaVersions)

lazy val gitToolProject = Project(
  "scala-forklift-git-tools", file("tools/git")).dependsOn(
  coreProject).settings(commonSettings:_*).settings(crossScalaVersions := supportedScalaVersions)
