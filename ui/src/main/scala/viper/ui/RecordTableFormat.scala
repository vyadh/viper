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

import ca.odell.glazedlists.gui.TableFormat
import viper.domain.{RecordPrototype, Record}

/**
 * Create table format from record prototype.
 * Column 0 is the Record object itself, so we can use it to style rows, etc.
 */
class RecordTableFormat(val prototype: RecordPrototype) extends TableFormat[Record] {

  val fields = prototype.fields

  def getColumnCount = fields.size + 1

  def getColumnName(column: Int) =
    if (column == 0) "base" else fields(column - 1).name

  def getColumnValue(baseObject: Record, column: Int): AnyRef = {
    if (column == 0) {
      baseObject
    } else {
      val field = fields(column - 1)
      field.value(baseObject)
    }
  }

  def defaultSort: List[(String, Boolean)] = prototype.defaultSort.map { s =>
    val (field, reverse) = s
    (field.name, reverse)
  }

}
