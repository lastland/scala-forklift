package scala.slick.jdbc.reflect
import scala.slick.jdbc.JdbcBackend
import scala.slick.jdbc.meta._

class Schema(tableNames : List[String])(implicit session:JdbcBackend#Session){
  def table(t:MTable) = new Table(this,t)
  def tables = MTable.getTables(None, None, None, None).list.filter(t => tableNames.contains(t.name.name)).map(table _)
}
trait ByValueComparison{
  protected def _value : Any 
  override def equals(that:Any) = this.getClass == that.getClass && this._value == that.asInstanceOf[ByValueComparison]._value 
}
class Table(val schema:Schema,val table:MTable)(implicit session:JdbcBackend#Session) extends ByValueComparison{
  val s = schema
  val t = table
  def _value = (s,t) 
  def name = t.name.name
  def column(c:MColumn) = new Column(this,c)
  def columns = t.getColumns.list.map(column _)
  def primaryKey = {
    val pkey = t.getPrimaryKeys.list.map(_.column)
    columns.filter(c=>pkey.contains(c.name))
  }
  override def toString = s"Table(${name})"
}

class Column(val table:Table,val column:MColumn)(implicit session:JdbcBackend#Session) extends ByValueComparison{
  val t = table
  val c = column
  def _value = (t,c) 
  def name = c.name
  def autoInc = c.isAutoInc
  def primaryKey = t.primaryKey.contains(this)
  def nullable = c.nullable // FIXME: what is the difference between nullable and isNullable?
  def sqlType = c.sqlType
  def sqlTypeName = c.sqlTypeName // <- shouldn't this go into the code generator?
  def columnSize = c.size
  override def toString = s"Column(${name})"
}
