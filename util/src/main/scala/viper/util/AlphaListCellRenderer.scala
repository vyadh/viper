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
