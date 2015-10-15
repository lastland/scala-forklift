package com.liyaos.forklift.slick.tests.subprojects

import ammonite.ops._

object TestDir {
  def createTestDir(wd: Path) = {
    synchronized {
      new TestDir(wd)
    }
  }
}

class TestDir(wd: Path) {
  val path = Path(Path.makeTmp)
  val testPath = path/'test

  def setup() {
    cp(wd/'example, testPath)

    // rm updated migration files
    ls! testPath/'migrations/'src/'main/'scala/'migrations | rm
    write(testPath/'migrations/'src/'main/'scala/'migrations/"Summary.scala",
      "object MigrationSummary { }")
    // rm db
    ls! testPath |? (_.ext == "db") | rm
    // mkdir .db
    mkdir! testPath/".db"
    // rm .git
    rm! path/".git"
    // set up git ignore
    rm! path/".gitignore"
    cp(wd/".gitignore", path/".gitignore")
    // rm generated code
    rm! testPath/'generated_code/'src
  }

  def destroy() {
    rm! testPath
  }

  override def toString = path.toString
}
