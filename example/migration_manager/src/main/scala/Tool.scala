package example.migration.manager

import com.liyaos.migrations.slick.SlickRescueCommands
import com.liyaos.migrations.slick.SlickRescueCommandLineTool

object Tool extends App
    with SlickRescueCommandLineTool
    with SlickRescueCommands
    with MyCodegen {
  execCommands(args.toList)
}
