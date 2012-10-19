package viper.ui

import javax.swing.JTable
import java.awt._
import viper.domain.Record
import viper.util.AlphaTableCellRenderer

class RecordTableCellRender extends AlphaTableCellRenderer {

  override def getTableCellRendererComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int): Component = {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

    val r = table.getModel.getValueAt(row, 0).asInstanceOf[Record]

    setForeground(r.severity.colour)

    this
  }

}
