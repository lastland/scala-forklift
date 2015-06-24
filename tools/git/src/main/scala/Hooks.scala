package scala.migrations.tools.git.hooks

import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.PosixFilePermissions

abstract class Script(dir: String) {
  def generate(): Unit

  protected def writeFileAndSetPermission(fileName: String, content: String) {
    val writer = new PrintWriter(fileName)
    writer.print(content)
    writer.close()
    Files.setPosixFilePermissions(Paths.get(fileName),
      PosixFilePermissions.fromString("rwxr-xr-x"))
  }
}

class Commands(val dir: String, gitToolDir: String, gitToolProject: String)
    extends Script(dir) {
  override def generate() {
    val content = s"""
#!/usr/local/bin/python
tool_dir = "$gitToolDir"
tool_running_command = "$gitToolProject/run"
def commit_command(commit_id):
    return " ".join(["cd", tool_dir, "; sbt '", tool_running_command, "commit", commit_id, "'"])
"""
    val fileName = dir + "/hooks/commands.py"
    writeFileAndSetPermission(fileName, content)
  }
}

class PostCommitHook(dir: String) extends Script(dir) {
  override def generate() {
    val content = s"""
#!/usr/local/bin/python
from commands import commit_command
import subprocess
log = subprocess.check_output("git log -1 HEAD".split())
commit_id = log.split("\n")[0][7:]
subprocess.call(commit_command(commit_id), shell=True)
"""
    val fileName = dir + "/hooks/post_commit"
    writeFileAndSetPermission(fileName, content)
  }
}
