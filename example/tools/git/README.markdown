# The Git Tool

This is a demo to show how to use the git tools provided with our migration tool.

## Installation

The git tools provide a set of git hooks. To install these hooks, simply provide correct arguments to the `install` method of `import scala.migrations.com.liyaos.forklift.slick.tools.git.Installer`. The example code can be found in file `GitUtil.scala`::

    class MyGitUtil(db: MyMigrationDatabase)
      extends Git(db, System.getProperty("user.dir") + "/../.git") {
      override def run(args: List[String]) {
         args match {
            case "install" :: Nil =>
                val currentDir = System.getProperty("user.dir")
                Installer.install(currentDir + "/../.git", currentDir, "git-tools")
            case _ =>
                super.run(args)
         }
      }
   }

## Usage

The git tools provide the following four hooks: `post-commit`, `post-merge`, `post-checkout`, `post-rewrite`. Normally, the user does not need to care about these hooks or what should be done after some git operations, but they do have to manually execute a `rebuild` command in one case: `git reset`.

Because git does not provide a hook around `reset`, and, in my opinion, the whole migration tool should avoid influencing the application code (e.g. the application code should look exactly the same with or without our migration tool), I intentionally leave the responsibility to execute this command to the users.

## What Does it Do?

The git tools provide three methods (see `MigrationDatabase`): `copy`, `use`, and `rebuild`. `copy` will copy your current database to the db storage. `use` does the opposite: it copies a specific database from db storage to your workspace. In case there's no database available to use, a `rebuild` should be called to rebuild the database using all the migration files.

These three methods are used by the four git hooks provided by our git tools. To be specific: `post-commit` hook will call `copy` to store your current database; `post-checkout` hook will call `use` to find the database corresponding the new commit id and branch name; `post-merge` will call `use` to use the database from the first parent of current merge commit, and call `copy` to store it to the merge commit; `post-rewrite` is the same as `post-checkout`, but it's only invoked in a `rebase` operation.

The users can implement the three methods by themselves, or use third-party  implementations. The file `GitUtil.scala` in this project gives a simple example:  it only stores a copy of the h2 database file per branch. However, developers can choose to implement versions to store a dabatase per commit, given that the commit id is also passed to these methods.

## Known Problems

- `sbt` must be invoked in all four git hooks, this could make it much slower.
