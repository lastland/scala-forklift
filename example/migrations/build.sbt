name := "migrations-example-migrations"

resolvers ++= List(
  Resolver.sonatypeRepo("snapshots")
//  ,Resolver.bintrayRepo("naftoligug", "maven")
)

libraryDependencies ++= List(
  "com.liyaos" %% "scala-forklift-slick" % "0.1.0-SNAPSHOT"
//  ,"io.github.nafg" %% "slick-migration-api" % "0.1.1"
  ,"com.zaxxer" % "HikariCP" % "2.3.9"
)
