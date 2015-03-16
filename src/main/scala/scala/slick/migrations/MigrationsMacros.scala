package scala.slick.migrations
import scala.slick.driver.H2Driver.simple._
import Database.dynamicSession
import scala.language.experimental.macros

abstract class GenericMigration[T]( val id:T )(f : Session => Unit) extends Migration[T]{
  def up : Unit = f(dynamicSession)
  def code : String
}

object GenericMigration extends GenericMigrationMacro // comment out all usages when compiling this
//object GenericMigration extends GenericMigrationFunction

trait GenericMigrationFunction{
  def apply[T]( id:T )(f : Session => Unit) = new GenericMigration[T](id)(f){
    def code = "(Scala source code preview requires GenericMigration extends GenericMigrationMacro but it currently extends GenericMigrationFunction.)"
  }
}
// GenericMigrationPreviewMacros
trait GenericMigrationMacro{ 
  def apply[T]( id:T )(f : Session => Unit) = macro GenericMigrationMacros.impl[T]
}
object GenericMigrationMacros{
  def impl[T:c.WeakTypeTag](c: scala.reflect.macros.Context)(id: c.Expr[T])(f : c.Expr[Session => Unit]) = {
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
      case Block(_,Function(_,Block(statements,expr))) => (statements.toList :+ expr).flatMap(t => makeMoreReadable(t).toString.split("\t")).mkString("\n\t")
      case Function(_,Block(statements,expr))          => (statements.toList :+ expr).flatMap(t => makeMoreReadable(t).toString.split("\t")).mkString("\n\t")
      case Function(_,statement)                                   => List(statement).flatMap(t => makeMoreReadable(t).toString.split("\t")).mkString("\n\t")
      case tree => showRaw(tree)
    }).toString)))
    reify{
      new GenericMigration[T](id.splice)(f.splice){
        def code = code_.splice
      }
    }
  }
}
