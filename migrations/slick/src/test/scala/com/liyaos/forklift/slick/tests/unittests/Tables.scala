package com.liyaos.forklift.slick.tests.unittests

import slick.jdbc.JdbcProfile

case class UsersRow(id: Int, first: String, last: String)

trait Tables {
  val profile: JdbcProfile

  import profile.api._

  class UsersV2(tag: Tag) extends Table[UsersRow](tag, "users") {
    def * = (id, first, last) <> (UsersRow.tupled, UsersRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val first: Rep[String] = column[String]("first")
    val last: Rep[String] = column[String]("last")
  }

  lazy val UsersV2 = new TableQuery(tag => new UsersV2(tag))

  class UsersV3(tag: Tag) extends Table[UsersRow](tag, "users") {
    def * = (id, firstname, lastname) <> (UsersRow.tupled, UsersRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val firstname: Rep[String] = column[String]("firstname")
    val lastname: Rep[String] = column[String]("lastname")
  }

  lazy val UsersV3 = new TableQuery(tag => new UsersV3(tag))
}
