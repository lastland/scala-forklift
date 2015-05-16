package example.migration.manager

import scala.migrations.slick.SlickCodegen

// override the default code generator here
trait MyCodegen extends SlickCodegen {
  // change directory of generated file options here
  // override val generatedDir = ...
  // override val container = ...
  // override val fileName = ...
}
