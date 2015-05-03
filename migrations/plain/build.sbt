organization := "com.typesafe"

name := "plain-migrations"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

scalacOptions += "-feature"

libraryDependencies ++= List(
  "org.scala-lang" % "scala-compiler" % "2.11.6"
  ,"org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
/*
// enables logging
  ,"org.slf4j" % "slf4j-api" % "1.6.4"
 ,"ch.qos.logback" % "logback-classic" % "0.9.28"
*/
)
