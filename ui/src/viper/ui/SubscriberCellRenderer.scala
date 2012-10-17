package viper.ui

import javax.swing.{ListCellRenderer, JLabel, JList, DefaultListCellRenderer}
import viper.domain.Subscriber
import java.awt.Component

class SubscriberCellRenderer extends ListCellRenderer[Subscriber] {

  val underlying = new DefaultListCellRenderer

  def getListCellRendererComponent(list: JList[_ <: Subscriber], value: Subscriber, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean): Component = {

    val result = underlying
          .getListCellRendererComponent(list, value.asInstanceOf[Any], index, isSelected, cellHasFocus)
          .asInstanceOf[JLabel]

    result.setText(value.name)

    result
  }

}
