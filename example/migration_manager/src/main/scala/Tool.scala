package example.migration.manager

import scala.migrations.slick.SlickRescueCommands
import scala.migrations.slick.SlickRescueCommandLineTool

object Tool extends App
    with SlickRescueCommandLineTool
    with SlickRescueCommands
    with MyCodegen {
  execCommands(args.toList)
}
