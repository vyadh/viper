package viper.util

import javax.swing.{JTable, UIManager}
import javax.swing.table.TableCellRenderer
import java.awt._

class AlphaTableCellRenderer extends AlphaCellRenderer with TableCellRenderer {

  val selectionBackground = UIManager.getColor("Table[Enabled+Selected].textBackground")

  def getTableCellRendererComponent(
    table: JTable,
    value: Any,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int): Component = {

    setText(value.toString)

    // Use paintComponent to paint the selection
    selected = isSelected

    this
  }

}
