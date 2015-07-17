package scala.migrations.tools.git.hooks

import scala.sys.process._
import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.PosixFilePermissions

abstract class Script(dir: String) {
  val content: String
  val fileName: String

  def generate() {
    writeFileAndSetPermission(fileName, content)
  }

  protected def writeFileAndSetPermission(fileName: String, content: String) {
    val writer = new PrintWriter(fileName)
    writer.print(content)
    writer.close()
    Files.setPosixFilePermissions(Paths.get(fileName),
      PosixFilePermissions.fromString("rwxr-xr-x"))
  }
}

class Commands(sbtDir: String, val dir: String,
  gitToolDir: String, gitToolProject: String)
    extends Script(dir) {
  override val content = s"""#!${"which python".!!.trim}
tool_dir = "${System.getProperty("user.dir")}"
tool_running_command = "git-tools/run"
sbt = "$sbtDir"

def common_command(command, branch, commit_id):
    return " ".join(["cd", tool_dir, "; ", sbt," '", tool_running_command, command, branch, commit_id, "'"])

def commit_command(branch, commit_id):
    return common_command("commit", branch, commit_id)

def merge_command(branch, commit_id):
    return common_command("merge", branch, commit_id)

def checkout_command(branch, commit_id):
    return common_command("checkout", branch, commit_id)

def rewrite_command(branch, commit_id):
    return common_command("rewrite", branch, commit_id)
"""
  override val fileName = dir + "/hooks/commands.py"
}

abstract class CommandHook(dir: String) extends Script(dir) {
  protected def scriptHead = s"""#!${"which python".!!.trim}\n"""
  protected def scriptImports(command: String) =
    s"""from commands import $command
import subprocess
"""
  protected def scriptBody(command: String) =
    s"""log = subprocess.check_output("git log -1 HEAD".split())
commit_id = log.split("\\n")[0][7:]
branch = subprocess.check_output("git rev-parse --abbrev-ref HEAD".split())
subprocess.call($command(branch, commit_id), shell=True)
"""

  def script(command: String) = List(
    scriptHead, scriptImports(command), scriptBody(command)).mkString("")
}

class PostCommitHook(dir: String) extends CommandHook(dir) {
  override val content = script("commit_command")
  override val fileName = dir + "/hooks/post-commit"
}

class PostMergeHook(dir: String) extends CommandHook(dir) {
  override val content = script("merge_command")
  override val fileName = dir + "/hooks/post-merge"
}

class PostCheckoutHook(dir: String) extends CommandHook(dir) {
  override val content = script("checkout_command")
  override val fileName = dir + "/hooks/post-checkout"
}

class PostRewriteHook(dir: String) extends CommandHook(dir) {
  override protected def scriptImports(command: String) =
    super.scriptImports(command) + "import sys\n"
  override protected def scriptBody(command: String) =
    s"""if sys.argv != "rebase":
    sys.exit(0)
""" + super.scriptBody(command)
  override val content = script("rewrite_command")
  override val fileName = dir + "/hooks/post-rewrite"
}
