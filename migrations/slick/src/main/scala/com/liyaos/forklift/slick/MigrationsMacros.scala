package com.liyaos.forklift.slick

import com.liyaos.forklift.core.Migration
import slick.dbio.DBIO

import scala.language.experimental.macros

abstract class DBIOMigration[T](val id:T)(f: DBIO[Unit])
    extends Migration[T, DBIO[Unit]] {
  def up: DBIO[Unit] = f
  def code: String
}

object DBIOMigration extends DBIOMigrationMacro // comment out all usages when compiling this
//object DBIOMigration extends DBIOMigrationFunction

trait DBIOMigrationFunction {
  def apply[T](id: T)(f: DBIO[Unit]) = new DBIOMigration[T](id)(f){
    def code = "(Scala source code preview requires DBIOMigration extends DBIOMigrationMacro but it currently extends DBIOMigrationFunction.)"
  }
}
// DBIOMigrationPreviewMacros
trait DBIOMigrationMacro {
  def apply[T](id:T)(f: DBIO[Unit]): DBIOMigration[T] =
    macro DBIOMigrationMacros.impl[T]
}

object DBIOMigrationMacros {
  def impl[T:c.WeakTypeTag](c: scala.reflect.macros.whitebox.Context)(
    id: c.Expr[T])(f: c.Expr[DBIO[Unit]]) = {
    import c.universe._
    object makeMoreReadable extends Transformer {
      def apply( tree:Tree ) = transform(tree)
      override def transform(tree: Tree): Tree = {
        super.transform {
          tree match {
            case Apply(TypeApply(Select(_, name), _), List(table)) if name.toString == "columnBaseToInsertInvoker" => makeMoreReadable(table)
            case Apply(tree,List(Ident(name))) if name.toString == "session" => makeMoreReadable(tree)
            case Select(Select(Select(Select(Ident(datamodel), _), schema), entities_or_tables), table)
              if datamodel.toString == "datamodel" && schema.toString == "schema" &&
                 (entities_or_tables.toString == "entities" || entities_or_tables.toString == "tables")
              => makeMoreReadable(Ident(table))
            case Select(tree, name) if name.toString == "apply" => makeMoreReadable(tree)
            case This(tree) => Ident(tree)
            case _ => tree
          }
        }
      }
    }
    val code_ = c.Expr[String](Literal(Constant((f.tree match {
      case Block(_,Function(_,Block(statements,expr))) =>
        (statements.toList :+ expr).flatMap { t =>
          makeMoreReadable(t).toString.split("\t")
        }.mkString("\n\t")
      case Function(_,Block(statements,expr)) =>
        (statements.toList :+ expr).flatMap { t =>
          makeMoreReadable(t).toString.split("\t")
        }.mkString("\n\t")
      case Function(_,statement) =>
        List(statement).flatMap { t =>
          makeMoreReadable(t).toString.split("\t")
        }.mkString("\n\t")
      case Apply(TypeApply(Select(Select(_, TermName(dbio)), _), _), statements)
          if dbio == "DBIO" =>
        statements.flatMap { t =>
          makeMoreReadable(t).toString.split("\t")
        }.mkString("\n\t")
      case tree => showRaw(tree)
    }).toString)))
    reify{
      new DBIOMigration[T](id.splice)(f.splice){
        def code = code_.splice
      }
    }
  }
}
