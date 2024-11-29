name := "forklift-slick-example"

addCommandAlias("mgm", "migration_manager/run")

addCommandAlias("mg", "migrations/run")


lazy val slickVersion = "3.5.2"

lazy val forkliftVersion = "0.3.3-SNAPSHOT"

lazy val commonSettings = Seq(
  organization := "com.liyaos",
  version := "2.0",
  scalaVersion := "2.13.14",
  scalacOptions += "-deprecation",
  scalacOptions += "-feature",
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
  resolvers += Resolver.jcenterRepo,
)

lazy val loggingDependencies = List(
  "org.slf4j" % "slf4j-nop" % "2.0.16" // <- disables logging
)

lazy val slickDependencies = List(
  "com.typesafe.slick" %% "slick" % slickVersion
)

lazy val dbDependencies = List(
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.h2database" % "h2" % "2.3.232"
)

lazy val forkliftDependencies = List(
  "com.liyaos" %% "scala-forklift-slick" % forkliftVersion
)

lazy val appDependencies = dbDependencies ++ loggingDependencies

lazy val migrationsDependencies =
  dbDependencies ++ forkliftDependencies ++ loggingDependencies

lazy val migrationManagerDependencies = dbDependencies ++ forkliftDependencies

lazy val example = Project("example", file(".")).aggregate(
  app, migrations, migrationManager, generatedCode, tools).settings(
  commonSettings:_*)

lazy val app = Project("app",
  file("app")).dependsOn(generatedCode).settings(
  commonSettings:_*).settings {
  libraryDependencies ++= appDependencies
}

lazy val migrationManager = Project("migration_manager",
  file("migration_manager")).settings(
  commonSettings:_*).settings {
  libraryDependencies ++= migrationManagerDependencies
}

lazy val migrations = Project("migrations",
  file("migrations")).dependsOn(
  generatedCode, migrationManager).settings(
  commonSettings:_*).settings {
  libraryDependencies ++= migrationsDependencies
}

lazy val tools = Project("git-tools",
  file("tools/git")).settings(commonSettings:_*).settings {
  libraryDependencies ++= forkliftDependencies ++ List(
    "com.liyaos" %% "scala-forklift-git-tools" % forkliftVersion,
    "com.typesafe" % "config" % "1.3.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
  )
}

lazy val generatedCode = Project("generate_code",
  file("generated_code")).settings(commonSettings:_*).settings {
  libraryDependencies ++= slickDependencies
}
