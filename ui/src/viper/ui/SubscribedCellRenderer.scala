package viper.ui

import javax.swing.{ListCellRenderer, JLabel, JList, DefaultListCellRenderer}
import java.awt.Component

class SubscribedCellRenderer extends ListCellRenderer[Subscribed] {

  val underlying = new DefaultListCellRenderer

  def getListCellRendererComponent(list: JList[_ <: Subscribed], value: Subscribed, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean): Component = {

    val result = underlying
          .getListCellRendererComponent(list, value.asInstanceOf[Any], index, isSelected, cellHasFocus)
          .asInstanceOf[JLabel]

    val unreadCount = value.unread.getValue
    val unreadText = if (unreadCount == 0) "" else " (" + unreadCount + ')'
    result.setText(value.subscriber.name + unreadText)

    result
  }

}
