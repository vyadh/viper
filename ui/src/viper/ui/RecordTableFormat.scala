package viper.ui

import ca.odell.glazedlists.gui.TableFormat
import viper.domain.{RecordPrototype, Record}

class RecordTableFormat(val prototype: RecordPrototype) extends TableFormat[Record] {

  val fields = prototype.fields

  def getColumnCount = fields.size

  def getColumnName(column: Int) = fields(column).name

  def getColumnValue(baseObject: Record, column: Int): AnyRef = {
    val field = fields(column)
    field.value(baseObject)
  }

}
