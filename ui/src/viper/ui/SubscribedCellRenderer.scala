package viper.ui

import javax.swing.{ListCellRenderer, JLabel, JList, DefaultListCellRenderer}
import java.awt.Component
import viper.util.AlphaListCellRenderer

class SubscribedCellRenderer extends AlphaListCellRenderer[Subscribed] {

  override def getListCellRendererComponent(list: JList[_ <: Subscribed], value: Subscribed, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean): Component = {

    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

    setForeground(value.severity.colour)

    val unreadText = if (value.unread == 0) "" else " (" + value.unread + ')'
    setText(value.subscriber.name + unreadText)

    this
  }

}
