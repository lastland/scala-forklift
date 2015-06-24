package scala.migrations.tools.git

import scala.migrations.tools.git.hooks._

trait Installer {
  def install(gitDir: String, gitToolDir: String, gitToolProjectName: String) {
    val commands = new Commands(gitDir, gitToolDir, gitToolProjectName)
    commands.generate()
    val postCommit = new PostCommitHook(gitDir)
    postCommit.generate()
  }
}

object Installer extends Installer
