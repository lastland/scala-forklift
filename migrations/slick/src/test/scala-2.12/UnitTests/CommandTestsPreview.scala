package com.liyaos.forklift.slick.tests.unittests

object CommandTestsPreview {

  val CommandTests_PreviewSeq: Seq[String] = List(
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

--------------------------------------------------------------------------------
""",
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

1 SqlMigration:
${"\t"}[create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR(255) NOT NULL,"last" VARCHAR(255) NOT NULL)]

--------------------------------------------------------------------------------
""",
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

2 DBIOMigration:
${"\t"}CommandTests.profile.api.queryInsertActionExtensionMethods[CommandTests.this.UsersV2#TableElementType, Seq](CommandTests.UsersV2).++=(scala.collection.Seq[com.liyaos.forklift.slick.tests.unittests.UsersRow](UsersRow(1, "Chris", "Vogt"), UsersRow(2, "Yao", "Li")))

--------------------------------------------------------------------------------
""")
  
  val CommandTests_PreviewSeq_MySql = List(
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

--------------------------------------------------------------------------------
""",
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

1 SqlMigration:
${"\t"}[create table `users` (`id` INTEGER NOT NULL PRIMARY KEY,`first` VARCHAR(255) NOT NULL,`last` VARCHAR(255) NOT NULL)]

--------------------------------------------------------------------------------
""",
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

2 DBIOMigration:
${"\t"}MySQLCommandTests.profile.api.queryInsertActionExtensionMethods[MySQLCommandTests.this.UsersV2#TableElementType, Seq](MySQLCommandTests.UsersV2).++=(scala.collection.Seq[com.liyaos.forklift.slick.tests.unittests.UsersRow](UsersRow(1, "Chris", "Vogt"), UsersRow(2, "Yao", "Li")))

--------------------------------------------------------------------------------
""")

  val CommandTests_PreviewSeq_Derby = List(
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

--------------------------------------------------------------------------------
""",
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

1 SqlMigration:
${"\t"}[create table "users" ("id" INTEGER NOT NULL PRIMARY KEY,"first" VARCHAR(255) NOT NULL,"last" VARCHAR(255) NOT NULL)]

--------------------------------------------------------------------------------
""",
    s"""--------------------------------------------------------------------------------
NOT YET APPLIED MIGRATIONS PREVIEW:

2 DBIOMigration:
${"\t"}DerbyCommandTests.profile.api.queryInsertActionExtensionMethods[DerbyCommandTests.this.UsersV2#TableElementType, Seq](DerbyCommandTests.UsersV2).++=(scala.collection.Seq[com.liyaos.forklift.slick.tests.unittests.UsersRow](UsersRow(1, "Chris", "Vogt"), UsersRow(2, "Yao", "Li")))

--------------------------------------------------------------------------------
""")
}
