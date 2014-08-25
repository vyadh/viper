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

import javax.swing. JTable
import java.awt._
import viper.domain.{Record, Readable}
import viper.util.AlphaTableCellRenderer
import java.text.SimpleDateFormat

class RecordTableCellRender extends AlphaTableCellRenderer {

  private val fontNorm = getFont
  private val fontBold = fontNorm.deriveFont(Font.BOLD)
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

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

    if (value.isInstanceOf[java.util.Date]) {
      setText(dateFormat.format(value))
    }

    this
  }

  def read(record: Record) = record match {
    case r: Readable => Some(r.read)
    case _ => None
  }

}
