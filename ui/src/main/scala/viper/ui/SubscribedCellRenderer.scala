/*
 * Copyright 2012-2014 Kieron Wilkinson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
