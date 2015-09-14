name := "scala-forklift-slick"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0"
  ,"com.typesafe.slick" %% "slick-codegen" % "3.0.0"
  ,"org.scala-lang" % "scala-compiler" % "2.11.6"
  ,"com.typesafe" % "config" % "1.3.0"
  ,"org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
  ,"org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
  ,"commons-io" % "commons-io" % "2.4" % "test"
)
