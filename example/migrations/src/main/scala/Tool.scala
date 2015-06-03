import scala.migrations.slick._

object Tool extends App
    with SlickMigrationCommandLineTool
    with SlickMigrationCommands
    with Migrations
    with Codegen {
  execCommands(args.toList)
}
