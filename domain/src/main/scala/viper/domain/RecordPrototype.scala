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
package viper.domain

/**
 * Described the fields on a record, ultimately used to layout a table.
 * @tparam T a prototype has fields that are specific to the record implementation
 */
trait RecordPrototype {

  /** Fields that are expected to be shown as columns on the view table. */
  def fields: Array[RecordField]

  /** Default sorting fields, and whether to reverse the sort order (true) or not (false) */
  def defaultSort: List[(RecordField, Boolean)]

}
