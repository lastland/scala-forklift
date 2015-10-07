name := "scala-forklift-slick"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0"
  ,"com.typesafe.slick" %% "slick-codegen" % "3.0.0"
  ,"org.scala-lang" % "scala-compiler" % "2.11.6"
  ,"com.typesafe" % "config" % "1.3.0"
  ,"org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
  ,"org.scalatest" %% "scalatest" % "2.2.4" % "test"
  ,"com.lihaoyi" %% "ammonite-ops" % "0.4.7" % "test"
  ,"commons-io" % "commons-io" % "2.4" % "test"
  ,"com.zaxxer" % "HikariCP" % "2.4.1" % "test"
  ,"com.h2database" % "h2" % "1.3.166" % "test"
  ,"org.xerial" % "sqlite-jdbc" % "3.8.11.2" % "test"
)

parallelExecution in Test := false
