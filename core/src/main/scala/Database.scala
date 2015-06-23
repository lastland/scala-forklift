package scala.migrations

abstract class MigrationDatabase {
  def copy(commitId: String): Unit
  def use(mainBranchId: String): Unit
}
