import example.migration.manager.MyCodegen

// override the default code generator here
trait Codegen extends MyCodegen {
  // set the models requiring code generation here
  override def tableNames = List("users")
}
