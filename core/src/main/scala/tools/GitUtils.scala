package com.liyaos.forklift.core.tools

import java.io.File
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import com.liyaos.forklift.core.MigrationDatabase

class GitUtil(db: MigrationDatabase, gitLoc: String) {
  lazy val repo =
    new FileRepositoryBuilder().setGitDir(
      new File(gitLoc)).readEnvironment().findGitDir().build()

  protected def findCommit(commitId: String) = {
    val objectReader = repo.newObjectReader()
    val objectLoader = objectReader.open(ObjectId.fromString(commitId))
    RevCommit.parse(objectLoader.getBytes)
  }

  def postCommit(branch: String, commitId: String) = {
    db.copy(branch, commitId)
  }

  def postMerge(branch: String, commitId: String) = {
    def mergeCommit = findCommit(commitId)
    def firstParentId = ObjectId.toString(mergeCommit.getParent(0))
    db.use(branch, firstParentId)
    db.copy(branch, commitId)
  }

  def postCheckout(branch: String, commitId: String) = {
    db.use(branch, commitId)
  }

  def postRewrite(branch: String, commitId: String) = {
    db.use(branch, commitId)
  }

  def run(args: List[String]) = args match {
    case "commit" :: branch :: id :: Nil =>
      postCommit(branch, id)
    case "merge" :: branch :: id :: Nil =>
      postMerge(branch, id)
    case "checkout" :: branch :: id :: Nil =>
      postCheckout(branch, id)
    case "rewrite" :: branch :: id :: Nil =>
      postRewrite(branch, id)
    case _ =>
      println("Unknown command!")
  }
}
