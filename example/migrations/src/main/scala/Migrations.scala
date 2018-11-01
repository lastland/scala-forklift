import com.liyaos.forklift.slick._
import example.migration.manager.MyMigrationManager

object MyMigrations extends App
    with SlickMigrationCommandLineTool
    with SlickMigrationCommands
    with MyMigrationManager
    with Codegen {
  MigrationSummary
  execCommands(args.toList)
}
