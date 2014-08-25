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

import viper.domain.{Severity, Record}
import java.util.Date

/**
 * A log record for java.util.logging XML output.
 */
case class JULLogRecord(
      id: String,
      timestamp: Long,
      sequence: Integer,
      level: String,
      severity: Severity,
      message: String) extends Record {

  def datetime = new Date(timestamp)
  def body = message

}
