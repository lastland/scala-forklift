import com.liyaos.forklift.slick._

object MyMigrations extends App
    with SlickMigrationCommandLineTool
    with SlickMigrationCommands
    with SlickMigrationManager
    with Codegen {
  MigrationSummary
  execCommands(args.toList)
}
