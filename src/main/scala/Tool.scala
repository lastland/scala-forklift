import scala.slick.migrations._
object Tool extends App{
  args.toList match{
    case "check" :: Nil =>
      val ny = SampleMigrations.notYetAppliedMigrations
      if( ny.size == 0 )
        println("your database is up-to-date")
      else
        println("your database is outdated, no yet applied migrations: "+SampleMigrations.notYetAppliedMigrations.map(_.id).mkString(", "))
    case "up" :: Nil =>
      println("applying migrations: "+SampleMigrations.notYetAppliedMigrations.map(_.id).mkString(", "))
      SampleMigrations.up
    case "preview" :: Nil =>
      SampleMigrations.notYetAppliedMigrations.foreach{
        migration =>
          println( "Migration "+migration.id+":")
          migration match{
          case m:SqlMigration[_] => 
            m.queries.map("\t"+_).foreach(println)
          case m:GenericMigration[_] =>
            println("\tgeneric migration, SQL not certain")
        }
      }
    case "init" :: Nil  => SampleMigrations.init
    case "reset" :: Nil => SampleMigrations.reset
    case _ =>
      val mm = SampleMigrations
      println( mm.alreadyAppliedIds )
      println( mm.notYetAppliedMigrations.map(_.id) )
      mm.up
      println( mm.alreadyAppliedIds )
  }
}
