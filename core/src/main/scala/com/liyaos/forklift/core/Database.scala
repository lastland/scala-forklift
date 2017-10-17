package com.liyaos.forklift.core

abstract class MigrationDatabase {
  def copy(branch: String, commitId: String): Unit
  def use(branch: String, commitId: String): Unit
  def rebuild(branch: String, commitId: String): Unit
}
