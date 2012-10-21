package viper.ui

import javax.swing. JTable
import java.awt._
import viper.domain.{Record, Readable}
import viper.util.AlphaTableCellRenderer

class RecordTableCellRender extends AlphaTableCellRenderer {

  val fontNorm = getFont
  val fontBold = fontNorm.deriveFont(Font.BOLD)

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

    if (read(r)) {
      setFont(fontNorm)
      setBackground(ColorScheme.recordRead)
    } else {
      setFont(fontBold)
      setBackground(ColorScheme.recordUnread)
    }

    this
  }

  def read(record: Record) = record match {
    case r: Readable => r.read
    case _ => true
  }

}
