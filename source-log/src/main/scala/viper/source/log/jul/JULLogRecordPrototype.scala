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
package viper.source.log.jul

import viper.domain.{Record, RecordField, RecordPrototype}

object JULLogRecordPrototype extends RecordPrototype {

  private val time = new RecordField("Time", convert(_).datetime)
  private val sequence = new RecordField("Seq", convert(_).sequence)

  def fields = Array(
    time,
    sequence,
    new RecordField("Level", convert(_).level),
    new RecordField("Message", convert(_).message)
  )

  def defaultSort = List(
    (time, true),
    (sequence, true)
  )

  private def convert(record: Record): JULLogRecord = {
    try {
      record.asInstanceOf[JULLogRecord]
    } catch {
      case _: ClassCastException =>
        throw new IllegalArgumentException("Unsupported type: " + record)
    }
  }

}
