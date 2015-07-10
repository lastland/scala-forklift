organization := "com.typesafe"

name := "migrations-example-git-tool"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

libraryDependencies ++= List(
  "com.typesafe" %% "slick-migrations" % "1.0"
  ,"com.typesafe" %% "migrations-git-tool" % "1.0"
  ,"com.typesafe" % "config" % "1.3.0"
  ,"org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
)
