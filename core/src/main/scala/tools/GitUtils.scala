package scala.migrations.tools

import scala.migrations.MigrationDatabase

class GitUtil(db: MigrationDatabase) {
  def postCommit(commitId: String) = {
    db.copy(commitId)
  }

  def postMerge(mainBranchId: String) = {
    db.use(mainBranchId)
  }

  def run(args: List[String]) = args match {
    case "commit" :: id :: Nil =>
      postCommit(id)
    case "merge" :: id :: Nil =>
      postMerge(id)
    case _ =>
      println("Unknown command!")
  }
}
