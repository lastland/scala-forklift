package com.liyaos.forklift.tools.git

import com.liyaos.forklift.tools.git.hooks._

trait Installer {
  def install(sbtDir: String,
    gitDir: String, gitToolDir: String, gitToolProjectName: String) {
    val commands = new Commands(sbtDir, gitDir, gitToolDir, gitToolProjectName)
    commands.generate()
    (new PostCommitHook(gitDir)).generate()
    (new PostMergeHook(gitDir)).generate()
    (new PostCheckoutHook(gitDir)).generate()
    (new PostRewriteHook(gitDir)).generate()
  }
}

object Installer extends Installer
