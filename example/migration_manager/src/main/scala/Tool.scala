package example.migration.manager

import com.liyaos.forklift.slick.SlickRescueCommands
import com.liyaos.forklift.slick.SlickRescueCommandLineTool

object Tool extends App
    with SlickRescueCommandLineTool
    with SlickRescueCommands
    with MyCodegen {
  execCommands(args.toList)
}
