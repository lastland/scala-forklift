import sbt._
import Keys._

object AppBuild extends Build {
  lazy val app = Project("app",
    file("app")).dependsOn(generatedCode)

  lazy val migrationManager = Project("migration_manager",
    file("migration_manager"))

  lazy val migrations = Project("migrations",
    file("migrations")).dependsOn(generatedCode, migrationManager)

  lazy val tools = Project("git-tools",
    file("tools/git"))

  lazy val generatedCode = Project("generate_code",
    file("generated_code"))
}
