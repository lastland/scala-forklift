name := "migrations-example-app"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0"
  ,"com.zaxxer" % "HikariCP" % "2.3.9"
  ,"org.scala-lang" % "scala-compiler" % "2.11.6"
  ,"com.h2database" % "h2" % "1.3.166"
  ,"org.xerial" % "sqlite-jdbc" % "3.6.20"
  ,"org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
  /*
   // enables logging
   ,"org.slf4j" % "slf4j-api" % "1.6.4"
   ,"ch.qos.logback" % "logback-classic" % "0.9.28"
   */
)
