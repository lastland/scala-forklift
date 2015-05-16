package example.migration.manager

import scala.migrations.RescueCommandLineTool
import scala.migrations.slick.SlickRescueCommands

object Tool extends App
    with RescueCommandLineTool
    with SlickRescueCommands
    with MyCodegen {
  execCommands(args.toList)
}
