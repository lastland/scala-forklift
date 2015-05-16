import sbt._
import Keys._
import Tests._

object migrationBuild extends Build {
  lazy val coreProject = Project("migrations-core", file("core"))
  lazy val slickMigrationProject = Project(
    "slick-migrations", file("migrations/slick")) dependsOn(coreProject)
  lazy val plainMigrationProject = Project(
    "plain-migrations", file("migrations/plain")) dependsOn(coreProject)
}
