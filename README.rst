A proof of concept of a tool for data migrations
===========================================================
Feedback and comments are very welcome! Especially if your use case cannot be realized using this tool.

Abstract / Vision
------------------------------------
This is a proof of concept of a data migration tool, which eliminates redundant manual work and supports a convenient, minimal effort, reliable and (in comparison to its alternatives) safer migration process.

When an installation of an application is upgraded to a new version, which changes the data model or storage engine, the existing data of the installation needs to be migrated accordingly. This is usually done using migration scripts which can be applied to each installation (e.g. dev, staging, production, customer).

The data model of an application can manifest itself in several places, e.g. the database schema, corresponding case classes and Slick table objects. Some developers maintain the corresponding manifestations manually, e.g. change the name of a field in a class and then write a migration script to change the database schema accordingly. This logically redundant activity means not only unnecessary work, but also a source of potential bugs due to inconsistent changes.

This tools offers a framework, api and user interface for writing, managing, previewing and applying migration scripts. It includes a customizable code generator, which can generate code corresponding to the data model automatically and exactly in the shape a developer wants it. It relieves the developer of changing it by hand in correspondance to the migration script. The tool allows to write migration scripts using SQL, Slick queries or arbitrary Scala code. Unlike SQL, Slick queries are type-checked and allow abstraction over the used database engine. Importantly, this tool allows to collectively type-check and compile multiple migration scripts referring to different versions of the data model. Migration scripts written in arbitrary Scala code allow for complex changes like relocating data from the database into the file-system. Like other migration tools, this tools also helps developers by providing ready, unified way to review and apply migrations, so that they do not have to design a process of their own.

Note: Some developers feel uncomfortable with code generation and would rather change their classes by hand. To reduce the problem of having to write logically redundant migration scripts, some envision generating these scripts by analysing the code changes afterwards. We don't think this is feasible because it only works for very simple cases like added classes or fields. But already renaming fields cannot be distinguished from removing one field and adding another one. More complex changes like splitting of tables or columns can hardly be detected automatically. The approach is limited and would require manual review and enhancement of migration scripts.

Limitations of the proof of concept
-----------------------------------------------------------------------
This implementation allows to get a feeling how working with such a software would be like, but is far from production ready. Plumbing, flexibility and details are not as they should be in the current state, especially regarding the numeric version numbers and all the hard coded things like the database connection etc. This only a rough demo.

Guarantees
-----------------------
This implementation guarantees (if no bugs) that:

- Migrations are applied in the right order.
- Migrations are not applied twice.
- Migrations are only applied on top of known migrations which have been applied in the right order. 
  Otherwise the application is rejected (which may prevent invalid states). 

Also an app can check that the code it uses matches the database schema version, so it does not run with incompatible versions.

Getting started / Demo
-----------------------------------------------------------------------
Demo steps (simplified output shown here; run ``run help`` for command descriptions)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. start ``sbt`` within the project folder
#. switch the project to ``app``
   ::
      > project app
#. the db is empty
   ::
      > run dbdump
#. initialize the database for migrations
   ::
      > run init
#. init created the __migrations__ table
   ::
      > run dbdump
      CREATE CACHED TABLE PUBLIC."__migrations__"(
          "id" INTEGER NOT NULL
      );
#. the migration yet to be applied
   ::
      > run status
      your database is outdated, not yet applied migrations: 1
#. its sql or scala code
   ::
      > run preview
      1 SqlMigration:
              create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR NOT NULL,"last" VARCHAR NOT NULL)
#. apply it
   ::
      > run apply
      applying migration 1
#. the db changed
   ::
      > run dbdump
      CREATE CACHED TABLE PUBLIC."__migrations__"(
          "id" INTEGER NOT NULL
      );
      INSERT INTO PUBLIC."__migrations__"("id") VALUES (1);
      CREATE CACHED TABLE PUBLIC."users"(
          "id" INTEGER NOT NULL,
          "first" VARCHAR NOT NULL,
          "last" VARCHAR NOT NULL
      );
#. generate the corresponding data model source files
   ::
      > run codegen
#. To simulate code evolution: uncomment code in `App.scala <https://github.com/cvogt/migrations/blob/a1acbfdad28b6efa0b7db1df7d1dc264a85818d4/src/main/scala/App.scala>`_
#. a yet empty list of users
   ::
      > run app
      Users in the database:
      List()
#. To simulate database evolution: uncomment code in `SampleMigrations.scala <https://github.com/cvogt/migrations/blob/a1acbfdad28b6efa0b7db1df7d1dc264a85818d4/src/main/scala/SampleMigrations.scala>`_
#. sql and scala code of migrations yet to be applied
   ::
      > run preview
      2 GenericMigration:
            Users.insertAll(User(1, "Chris", "Vogt"), User(2, "Stefan", "Zeiger"))

      3 SqlMigration:
            alter table "users" alter column "first" rename to "firstname"
            alter table "users" alter column "last" rename to "lastname"
#. the app runs fine as the version of the last generated code matches the current db version
   ::
      > run app
      Users in the database:
      List()
#. update, so the db version does not match anymore
   ::
      > run apply
      applying migration 2
      applying migration 3
#. the db changed
   ::
      > run dbdump
      CREATE CACHED TABLE PUBLIC."__migrations__"(
          "id" INTEGER NOT NULL
      );
      INSERT INTO PUBLIC."__migrations__"("id") VALUES (1),(2),(3);
      CREATE CACHED TABLE PUBLIC."users"(
          "id" INTEGER NOT NULL,
          "first" VARCHAR NOT NULL,
          "last" VARCHAR NOT NULL
      );
      INSERT INTO PUBLIC."users"("id", "firstname", "lastname") VALUES
         (1, 'Chris', 'Vogt'),
         (2, 'Stefan', 'Zeiger');
#. the app realizes it uses an out-dated data model
   ::
      > run app
      Generated code is outdated, please run code generator
#. re-generate data model classes
   ::
      > run codegen
#. finally we see the users added in migration 2
   ::
      > run app
      Users in the database:
      List(User(1,Chris,Vogt), User(2,Stefan,Zeiger))

Play around yourself
^^^^^^^^^^^^^^^^^^^^

- ``run help``
- write your own migrations `SampleMigrations.scala <https://github.com/cvogt/migrations/blob/a1acbfdad28b6efa0b7db1df7d1dc264a85818d4/src/main/scala/SampleMigrations.scala>`_
- change the demo app `App.scala <https://github.com/cvogt/migrations/blob/a1acbfdad28b6efa0b7db1df7d1dc264a85818d4/src/main/scala/App.scala>`_
- gather an understanding for the setup and the vision of this proof of concept :)

Pitfalls
-----------------
``macro implementation not found: ...``
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
try commenting out all migrations in SampleMigrations.scala, then compile, then uncomment the migrations again. Then try again. (sbt isn't setup to compile our macros independently by itself in this demo).

``org.h2.jdbc.JdbcSQLException: Table "__migrations__" not found``
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
run ``run init``

other compile errors
^^^^^^^^^^^^^^^^^^^^^^
You can always throw away all changes and get back to a working state by running ``git reset --hard && sbt "run reset" && sbt "run init"``.

Use cases (run ``run help`` for command descriptions)
-----------------------------------------------------------------------
#. Code developer who has full control over database (e.g. consumer app with embedded database, startups, small business, etc.)
    * Once, initially
        + ``run init`` to prepare the db for managing migrations.
        + ``run codegen``
    * Handle any kind of change (schema, content, file system, ...) exclusively(!) via migrations that
        + needs to be replicated in another installation (e.g. staging, production, customer installations, etc.)
        + cannot be covered by git alone (e.g. moving profile pictures out of db blob columns into files)
    * ``run preview`` for review purposes
    * ``run dbdump`` for backups before applying migrations
    * ``run apply`` to peform the upgrade
    * ``run codegen`` if necessary
   
   When merging changes from different developers ``run status`` and ``run preview`` allow to check for unapplied migrations.

#. Code developer can suggest changes to Database Architect (e.g. smaller enterprise environment)
    * ``run codegen`` when necessary
    * Occasionally write a database migration. Then use ``run preview`` and suggest the change to the Database Architect.
      Delete the migration afterwards or comment it out and put it under version control for documentation purposes.

#. Code Developer does not control database (e.g. enterprise environment)
    * ``run codegen`` when necessary.
    * Ignore migrations feature.

For upgrading an unaccessible remote installation (e.g. a software installation on a consumer pc), use the programmatic interface similar with similar steps like scenario 1.

Important notes
-----------------------------
Commit the generated code to your source control system as other people need it to compile your migrations ahead of applying them.

If code of older migrations ever becomes incompatible with a new version of Slick itself, delete or comment out the old migrations, but (!!) keep around an old binary of your app, which can upgrade old installations to a version which can then be upgraded by newer versions of your app.

Migrations are wrapped in database transactions automatically to prevent semi applied migrations. If you get an exception within a transaction the database state is rolled back. In migration script written in arbitrary Scala code, you need to take thatAny other changes you did to the file system or else, you have to recover yourself.

Currently, the generated data model code is versioned into packages, which means many old versions of the generated data model code will be stored in your code folders and should be versioned in your version control. When you commit a migration that changes the schema you SHOULD also commit the generated source for it. The reason is, that if you write migration code using Slick's type-safe database-independent API, older migrations will depend on older versions of your data model code. If that would not be available they could not be compiled anymore. If you are using only plain SQL migrations you can disable the generation of the version data model source files and always only ship the latest generated version, applying SQL migrations to achieve compatibility with it.

Future improvement ideas
-----------------------------
A SlickMigration, which takes type-safe Slick queries (instead of SQL or arbitrary code), but still allows to show or even store the generated SQL.
(either using a common api for getting it from different types of queries, like inserts, drops, etc. or by logging the generated queries in a rolled back transaction). The stored SQL could be put put in git and used itseld to apply the migration instead of running the Scala code snippet, which may give some people a feeling of more control over what is happening, especially with production databases, since they see the exact SQL not just the abstracted Slick query.

An SqlFileMigration, which takes SQL from a file instead of a String literal.

A Iterator which yields Migration objects based on SQL files in a certain directory, to support similar use to play's migration framework.

Maybe a way to dump migrations as a set of SQL script files, to feed Play's migration manager.

Upgrading to particular versions

A way to specify that data model classes are compatible with a range of database schema versions, not only one (for more flexible upgrade processes).

An option to NOT version generated code (by version we mean putting it into packages containing the version in the name)

Managing database changes in a development scenario with branches and distributed development
Code is typically developed using different branches and merging when certain features become stable. This is usually tricky with databases but we could offer significant support to ease the situation. We could offer an easy way to clone the (development) database, when branching off the (for instance) master branch. Migrations could be recorded independently in the master and a feature branch. When merging, the developer needs to put the migrations added in the master branch ahead of the migrations added in the feature branch, throw away the database clone, (if merging master into feature also create a new clone of the master database) and upgrade the db.

Version numbers should probably not be integers to avoid conflicts, especially in a branched development. Maybe even random numbers, hashes, version numbers with a versioning scheme (possibly containing branch names, or a notion of compatible or incompatible changes).

FIXME
---------------------
There are some dependencies on the order of results of the h2 database in some assert statements. This should not be the case.
And much more...
