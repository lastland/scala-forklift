import sbt._
import Keys._

object AppBuild extends Build {
  lazy val app = Project("app", file("app"))
  lazy val migrations = Project("migrations", file("migrations"))
}
