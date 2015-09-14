name := "migrations-example-migration-manager"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "3.0.0"
    ,"com.typesafe.slick" %% "slick-codegen" % "3.0.0"
    ,"org.scala-lang" % "scala-compiler" % "2.11.6"
    ,"com.h2database" % "h2" % "1.3.166"
    ,"org.xerial" % "sqlite-jdbc" % "3.6.20"
    ,"org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
    ,"com.liyaos" %% "scala-forklift-slick" % "0.2.0-SNAPSHOT"
    ,"com.zaxxer" % "HikariCP" % "2.3.9"
    /*
     // enables logging
     ,"org.slf4j" % "slf4j-api" % "1.6.4"
     ,"ch.qos.logback" % "logback-classic" % "0.9.28"
     */
)
