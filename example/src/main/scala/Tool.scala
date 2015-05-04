import scala.migrations.slick._
object Tool extends App{
  args.toList match{
    case "status" :: Nil =>
      val ny = SampleMigrations.notYetAppliedMigrations
      if( ny.size == 0 )
        println("your database is up-to-date")
      else
        println("your database is outdated, not yet applied migrations: "+SampleMigrations.notYetAppliedMigrations.map(_.id).mkString(", "))
    case "apply" :: Nil =>
      println("applying migrations: "+SampleMigrations.notYetAppliedMigrations.map(_.id).mkString(", "))
      SampleMigrations.up
    case "preview" :: Nil =>
      println("-" * 80)
      println("NOT YET APPLIED MIGRATIONS PREVIEW:")
      println("")
      SampleMigrations.notYetAppliedMigrations.map{
        migration =>
          migration match{
            case m:SqlMigration[_] =>
              println( migration.id+" SqlMigration:")
              println( "\t" + m.queries.mkString("\n\t") )
            case m:GenericMigration[_] =>
              println( migration.id+" GenericMigration:")
              println( "\t" + m.code )
          }
          println("")
      }
      println("-" * 80)
    case "init" :: Nil  => SampleMigrations.init
    case "reset" :: Nil => SampleMigrations.reset
    case "codegen" :: Nil =>
      SampleCodegen.gen( SampleMigrations ) // SampleMigrations is passed in here only because it contains the db connection
    case "dbdump" :: Nil =>
      import scala.slick.driver.H2Driver.simple._
      import Database.dynamicSession
      import scala.slick.jdbc.StaticQuery._
      SampleMigrations.db.withDynSession{
        println( queryNA[String]("SCRIPT").list.mkString("\n") )
      }
    case "app" :: Nil =>
      App.run( SampleMigrations ) // SampleMigrations is passed in here only because it contains the db connection
    case _ =>
      println("""
-------------------------------------------------------------------------------
A list of command available in this proof of concept:

  init      create the __migrations__ table which stores version information

  reset     totally clears the database and deletes auto-generated source files
            (this can be used to restart the demo and start again with init)

  app       run the demo app

  codegen   generate data model code (table objects, case classes) from the
            database schema

  status    display the migrations that have not been applied yet

  preview      display the migrations that have not been applied yet and show
            corresponding sql for sql migrations

  apply     apply all migrations which have not been applied yet
            (this could be extended to allow applying only migrations up to a
             stated migration but not further)

  dbdump    print a dump of the current database
-------------------------------------------------------------------------------
""".trim)

  }
}
