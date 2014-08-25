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
package viper.domain.log

import viper.domain.{Severity, Record, Readable}
import java.util.Date

/**
 * A log record may or may not be {@link com.pareto.viper.domain.Persistable}, it depends on the
 * Source, which may mix that trait in at runtime. E.g. Log content coming from JMS would be,
 * but not those coming from a log file.
 *
 * @param source
 * @param severity
 * @param body
 */
case class LogRecord(
      id: String,
      source: String,
      time: Date,
      severity: Severity,
      application: String,
      body: String,
      var read: Boolean) extends Record with Readable
