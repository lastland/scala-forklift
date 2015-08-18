name := "slick-migrations"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0"
  ,"com.typesafe.slick" %% "slick-codegen" % "3.0.0"
  ,"org.scala-lang" % "scala-compiler" % "2.11.6"
  ,"com.typesafe" % "config" % "1.3.0"
  ,"com.h2database" % "h2" % "1.3.166"
  ,"org.xerial" % "sqlite-jdbc" % "3.6.20"
  ,"org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
)
