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

    read(r) match {
      case Some(true) =>
        setFont(fontNorm)
        setBackground(ColorScheme.recordRead)
      case Some(false) =>
        setFont(fontBold)
        setBackground(ColorScheme.recordUnread)
      case None =>
        setFont(fontNorm)
        setBackground(ColorScheme.recordUnread)
    }

    this
  }

  def read(record: Record) = record match {
    case r: Readable => Some(r.read)
    case _ => None
  }

}
