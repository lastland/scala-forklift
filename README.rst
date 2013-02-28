A proof of concept to software upgrades / migrations
=========================================================

Feedback and comments are very welcome! Especially if your use case isn't covered by this scenario.

Abstract / Vision
------------------------------------
We demo a tool for convenient, easy, safe, stable and reliable handling of software upgrades including database migrations, that manages the kind of changes that your source code control system like git cannot by itself because they relate to installation specific data. Since generating upgrade scripts from comparing before and after snapshots of your project fails for anything but simple cases, we believe the solution is exclusively writing the upgrade scripts by hand and generating everything else from that.

Background
----------------------------------
We assume a software with several installations, at least development and production, but possibly also staging or even remote installations on consumer computers (e.g. for a tax app). Changes need to be able to be applied initially in development and later applied on different installations. For source code this is easy. You manage the code in git or else and ship a new jar to your customers at some point. For installation specific data (like user added files, database content, etc.) you need to run a script to manipulate it dynamically. The same for the database schema, because it is attached to the stored data and cannot simply be replaced like code. The scripts to perform these changes, we call migrations here. This software aims to manage migrations (semi-)automatically and avoid redundancy in writing them and maintaining the related code.

Schema aware database libraries like Slick have a representation of the data model that has to correspond to the actual database schema. The  representation is usually maintained using a description in form of configuration (e.g. xml,yml,...) or some kind API calls (scala sources). Managing this description and the actual database schema both by hand is redundant and thus entails unnecessary work and a source of mistakes and inconsistencies. To overcome this we need to only maintain one of the two and generate the changes to the other from it.

Scenario A (rejected)
We could compare the representation before and after a change and try to generate scripts to migrate the database accordingly. However this approach is very limited as we are comparing two static snapshots and it is not unambiguous how to get from one to the other. Renaming a column can not be distinguished from removing one column and adding another one. More complicated changes like splitting of tables or else cannot be clearly detected either. This approach may work for simple cases like added or deleted tables and columns, which may be the most common. But it surely needs manual (redundant) work whenever the change is ambigious or just more complex than supported.

Scenario B (chosen)
We describe how the database needs to change and then generate the description required by Slick to build its data model representation automatically from the database schema. This works without any complication (like fallbacks to manual work) with an out-of-the-box generator which could be shipped with Slick. However, with an out-of-the-box code-generator you obviously loose flexibilty regarding your code. This may be not be a problem in most cases at this is only the data model description code. But if you want to influence it (e.g. to map names in your own style, or add doc comments), it would be. The flexibility can however be achieved with a customizable code generator. In the most flexible case, Slick could just build a data model representation from the actualy database schema and hand it to a user provided script that can do arbitrary code generation or anything based on the data model.

We suggest that Scenario B is the most satisfying one of all choices.

Proof of concept implementation
-----------------------------------------------------------------------
This proof of concepts implements the previously describes Scenario B and helps in writing and managing the application of transactions and provides a flexible code generator, which should eventually be replaced by Slick type providers. It features diagnostics, semi-automatic upgrades and code-generation. This implementation allows to get a feeling how working with such a software would be like, but is far from production ready. Plumbing, flexibility and details are not as they should be in the current state, especially regarding the numeric version numbers and all the hard coded things like the database connection etc. This only a rough demo.

Guarantees
-----------------------
This implementation guarantees (if no bugs) that:
* Migrations are applied in the right order.
* Migrations are not applied twice.
* Upgrading databases which state an unexpected order of previously applied migrations is rejected (may be useful later in a branched dev scenario).

Requirements: Building Slick
-----------------------------------------------------------------------
This project was tested against a Slick revision which has not been released in binary form. You need build it yourself and publish it locally. To do this:
* `git clone git@github.com:slick/slick.git`
* `git checkout d84799440894370af14f06969dff5a354496bf55`
* `sbt publish-local` (You may have to configure the sbt-gpg plugin or gpg for this to work. Good luck :))

Getting started / Demo
-----------------------------------------------------------------------
Demo steps (run `run help` for command descriptions)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# start sbt within the project folder, then type:
# `run dbdump` to see the db is (almost) empty
# `run init` to initialize the (currently still empty) database for migrations
# `run dbdump` to see how init created the __migrations__ table 
# `run status` to see the migration yet to be applied
# `run diff` to see its sql or scala code
# `run up` to apply it
# `run dbdump` to see how the db changed
# `run codegen` to generate the corresponding data model source files
# To simulate code evolution: uncomment code in App.scala
# `run app` to see a yet empty list of users
# To simulate database evolution: uncomment code in SampleMigrations.scala
# `run diff` to see sql and scala code of migrations yet to be applied
# `run app` to see the app run fine as the version of the last generated code matches the current db version
# `run up` now the db version does not match anymore
# `run dbdump` to see how the db changed
# `run app` to see how the app realizes it uses an out-dated data model
# `run codegen` to upgrade the generated data model classes
# `run app` to finally see the users added in migration 2

Play around yourself
^^^^^^^^^^^^^^^^^^^^
* `run help`
* write your own migrations
* change the demo app
* gather an understanding for the setup and the vision of this proof of concept :)

Pitfalls
-----------------
`macro implementation not found: ...`
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
try commenting out all migrations in SampleMigrations.scala, then compile, then uncomment the migrations again. Then try again. (sbt isn't setup to compile our macros independently by itself in this demo).

`org.h2.jdbc.JdbcSQLException: Table "__migrations__" not found`
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
run `run init`

other compile errors
^^^^^^^^^^^^^^^^^^^^^^
You can always throw away all changes and get back to a working state by running `git reset --hard && sbt "run reset" && sbt "run init"`.

Work flow scenarios (run `run help` for command descriptions)
-----------------------------------------------------------------------
# Code developer who has full control over database (e.g. consumer app with embedded database, startups, small business, etc.)
   Once, initially
      `run init` to prepare the db for managing migrations.
      `run codegen`
   Handle any kind of change (schema, content, file system, ...) exclusively(!) via migrations that
      * needs to be replicated in another installation (e.g. staging, production, customer installations, etc.)
      * cannot be covered by git alone (e.g. moving profile pictures out of db blob columns into files)
   `run diff` for review purposes
   `run dbdump` for backups before applying migrations
   `run up` to peform the upgrade
   `run codegen` if necessary
   
   When merging changes from different developers `run status` and `run diff` allow to check for unapplied migrations.

# Code developer can suggest changes to Database Architect (e.g. smaller enterprise environment)
   `run codegen` when necessary
   Occasionally write a database migration. Then use `run diff` and suggest the change to the Database Architect.
   Delete the migration afterwards or comment it out and put it under version control for documentation purposes.

# Code Developer does not control database (e.g. enterprise environment)
   `run codegen` when necessary.
   Ignore migrations feature.

For upgrading an unaccessible remote installation (e.g. a software installation on a consumer pc), use the programmatic interface similar with similar steps like scenario 1.

Important notes
-----------------------------
Commit the generated data model source files to your source control system as other people need it to compile your migrations ahead of applying them.

If code of older migrations ever becomes incompatible with a new version of Slick, delete or comment out the old migrations, but (!!) keep around an old binary of your app, which can upgrade old clients to a version which can still be upgraded by newer versions of your app.

Migrations are wrapped in database transactions automatically. If you get an exception within a transaction the database state is rolled back. Any other changes you did to the file system or else, you have to recover yourself.

Currently, the generated data model code is versioned into packages, which means many old versions of the generated data model code will be stored in your code folders and should be versioned in your version control. When you commit a migration that changes the schema you SHOULD also commit the generated source for it. The reason is, that if you write migration code using Slick's type-safe database-independent API, older migrations will depend on older versions of your data model code. If that would not be available they could not be compiled anymore. If you are using only plain SQL migrations you can disable the generation of the version data model source files and always only ship the latest generated version, applying SQL migrations to achieve compatibility with it.

Future improvement ideas
-----------------------------
A SlickMigration, which takes type-safe Slick queries (instead of SQL or arbitrary code), but still allows to show or even store the generated SQL.
(either using a common api for getting it from different types of queries, like inserts, drops, etc. or by logging the generated queries in a rolled back transaction). The stored SQL could be put put in git and used itseld to apply the migration instead of running the Scala code snippet, which may give some people a feeling of more control over what is happening, especially with production databases, since they see the exact SQL not just the abstracted Slick query.

Upgrading to particular versions

An option to NOT version generated code (by version we mean putting it into packages containing the version in the name)

Managing database changes in a development scenario with branches and distributed development
Code is typically developed using different branches and merging when certain features become stable. This is usually tricky with databases but we could offer significant support to ease the situation. We could offer an easy way to clone the (development) database, when branching off the (for instance) master branch. Migrations could be recorded independently in the master and a feature branch. When merging, the developer needs to put the migrations added in the master branch ahead of the migrations added in the feature branch, throw away the database clone, (if merging master into feature also create a new clone of the master database) and upgrade the db.

Version numbers should probably not be integers to avoid conflicts, especially in a branched development. Maybe even random numbers, hashes, version numbers with a versioning scheme (possibly containing branch names, or a notion of compatible or incompatible changes).

FIXME
---------------------
There are some dependencies on the order of results of the h2 database in some assert statements. This should not be the case.