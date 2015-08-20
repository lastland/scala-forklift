organization := "com.liyaos"

name := "forklift-example-git-tool"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= List(
  "com.liyaos" %% "scala-forklift-slick" % "1.0-SNAPSHOT"
  ,"com.liyaos" %% "scala-forklift-git-tools" % "1.0-SNAPSHOT"
  ,"com.typesafe" % "config" % "1.3.0"
  ,"org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
)
