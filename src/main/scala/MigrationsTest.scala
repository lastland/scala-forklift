import scala.slick.migrations._
object Test extends App{

  import scala.slick.driver.H2Driver.simple._
  val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  val mm = new MigrationManager[Int]{
    val alreadyAppliedIds = collection.mutable.MutableList(1)
    def afterApply(migration:Migration[Int]) = {
      alreadyAppliedIds += migration.id
    }
    override def beforeApply(migration:Migration[Int]) = {
      println("applying migration "+migration.id)
    }
    override def up : Unit = {
      val ids = migrations.map(_.id)
      assert( ids == Range(1,ids.size+1).toList )
      super.up
    }
    def migrations  = List(
      // WARNING: never change to code of an already applied/published migration
      new GenericMigration( 1 )(
        session => ()
      ),
      new GenericMigration( 2 )(
        session => ()
      ),
      new GenericMigration( 3 )(
        session => ()
      )
    )
  }

  println( mm.alreadyAppliedIds )
  println( mm.notYetAppliedMigrations.map(_.id) )
  mm.up
  println( mm.alreadyAppliedIds )
}

/*package scala.slick.test.migrations
import scala.slick.migrations._
import scala.slick.testutil._
import scala.slick.testutil.TestDBs._
import scala.slick.session._
import org.junit.Test
import org.junit.Assert._
import com.typesafe.slick.testkit.util.TestDB
import scala.slick.driver.ExtendedProfile

object MigrationsTest extends DBTestObject(H2Mem, H2Disk, /*SQLiteMem, SQLiteDisk,*/ Postgres, MySQL, DerbyMem, DerbyDisk, HsqldbMem, MSAccess, SQLServer)
class MigrationsTest(val tdb: TestDB) extends DBTest {
  import tdb.profile._
  import tdb.profile.Implicit._
  object Tasks extends Table[(Int,String)]("tasks"){
    def id = column[Int]("id",O.PrimaryKey)
    def name = column[String]("name")
    def * = id ~ name
  }
  class MigrationTestException extends Exception
  @Test def testMigrations() {
    db.withSession{ implicit session:Session =>
      val events = new collection.mutable.MutableList[String]
      implicit val d = tdb.profile
      val m = new MigrationManager(
        List(
          upTo( 1 ){
            events += "a"
            Tasks.ddl.create
          },
          upTo( 2 ){
            events += "b"
            Tasks.insertAll(
              (1, "Task 1"),
              (2, "Task 2")
            )
          },
          upTo( 3 ){
            Tasks.insertAll(
              (3, "Task 3"),
              (4, "Task 4")
            )
            events += "c"
            throw new MigrationTestException // break transaction
          },
          upTo( 4 ){
            Tasks.insertAll(
              (5, "Task 5"),
              (6, "Task 6")
            )
          }
        )
      )
      
      // test transactions
      m.initialize
      assertEquals( m.lastApplied, m.initial )
      try{
        m.upgradeTo(3)
        fail("exception in upgrade suppressed")
      }catch{
        case _:MigrationTestException => 
      }
      // make sure all 3 migrations were executed
      assertEquals( events, List("a","b","c") )

      // make sure only the first two migrations affected the db (as the third had an exception)
      assertEquals( Tasks.sortBy(_.id).list, List((1,"Task 1"),(2,"Task 2")) )

      // make sure the the lastApplied 
      assertEquals( 2, m.currentVersion )
      
      // test passing invalid version numbers
      for( l <- List( List(-1), List(2,2), List(3,2) ))
        try{
          new MigrationManager( l.map(x => upTo(x){}) )
          fail("invalid version number not caught")
        }catch{
          case _: base.InvalidVersionNumbers => 
        }
    }
  }
}*/