package viper.util

import javax.swing.{JList, ListCellRenderer, UIManager}
import java.awt._

class AlphaListCellRenderer[T] extends AlphaCellRenderer with ListCellRenderer[T] {

  val selectionBackground = UIManager.getColor("List[Selected].textBackground")

  setBackground(UIManager.getColor("List.background").brighter)


  def getListCellRendererComponent(
    list: JList[_ <: T],
    value: T,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean): Component = {

    setText(value.toString)

    // Use paintComponent to paint the selection
    selected = isSelected

    this
  }

}
