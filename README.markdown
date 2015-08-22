# Scala-Forklift

Scala-Forklift helps manage and apply database migrations for your Scala project.

Key Features:

- Supports for Slick (and Casbah is on the way!).
- A source code generator to manage models of your database of all versions.
- A tool to help you manage your dev db with git.
- High customizability.

## How to Use

Add the following dependency to your `build.sbt`:

    resolvers += Resolver.sonatypeRepo("snapshots")

    libraryDependencies += "com.liyaos" %% "scala-forklift-slick" % "0.1.0-SNAPSHOT"

check [example](/example) for tutorial and example code.
