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
package viper.source.log.regular

import java.io.{BufferedReader, Reader}
import collection.mutable
import viper.domain._
import viper.source.log.jul.AbstractJULConsumer
import java.text.SimpleDateFormat
import viper.util.TimeoutTask

class JULSimpleConsumer(reader: => Reader) extends AbstractJULConsumer {

  /*
  Apr 02, 2013 9:58:34 AM viper.util.LogFileGenerator$ main
  WARNING: normal message
  Apr 02, 2013 9:58:34 AM viper.util.LogFileGenerator$ main
  SEVERE: my message
  java.lang.IllegalStateException: my exception
    at viper.util.LogFileGenerator$.main(LogFileGenerator.scala:17)
    at viper.util.LogFileGenerator.main(LogFileGenerator.scala)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:601)
    at com.intellij.rt.execution.application.AppMain.main(AppMain.java:120)
  Caused by: java.lang.Exception: my cause
    ... 7 more
  */

  /** E.g. Apr 02, 2013 9:58:34 AM */
  private val DateTimePatternJava7 = """([A-Z][a-z]{2} \d\d, \d{4} \d{1,2}:\d\d:\d\d [AP]M) .+""".r
  /** E.g. 04-Apr-2013 09:31:43 */
  private val DateTimePatternJava6 = """(\d\d-[A-Z][a-z]{2}-\d{4} \d\d:\d\d:\d\d) .+""".r
  /** E.g. INFO: message */
  private val LevelMessagePattern = """(FINEST|FINER|FINE|CONFIG|INFO|WARNING|SEVERE): (.+)""".r

  /** E.g. Apr 02, 2013 9:58:34 AM */
  private val dateFormatJava7 = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa")
  /** E.g. 04-Apr-2013 09:31:43 */
  private val dateFormatJava6 = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss")

  /** Implicit sequence value, used to order events with the same time. */
  private var sequence = 0


  def consume() {
    val bufferedReader = new BufferedReader(reader)

    processAllRecords(bufferedReader)
  }

  private def processAllRecords(reader: BufferedReader) {
    // Temporary storage for node values, needs to be cleared after each record
    val map = new mutable.HashMap[String, String]()

    // Action to take when we want to dispatch the next event
    val next = () => indicateNext(map)

    // Create fallback, so if event is not sent within 2 secs, it's likely the last one, so send on timeout
    val timeout = new TimeoutTask(1000)(next())

    try {

      val consumption = consumer(map, timeout, _: String)

      // Consume all lines
      var line: String = null
      do {
        line = reader.readLine()
        consumption(line)
      } while (line != null)

    } finally {
      // No longer need the timeout
      timeout.close()
    }
  }

  private def consumer(map: mutable.Map[String, String], timeout: TimeoutTask, line: String) {
    line match {
      case DateTimePatternJava7(dateStr) => {
        timeout.stage()
        map.put("millis", millisJava7(dateStr))
        map.put("sequence", nextSequence())
      }
      case DateTimePatternJava6(dateStr) => {
        timeout.stage()
        map.put("millis", millisJava6(dateStr))
        map.put("sequence", nextSequence())
      }
      case LevelMessagePattern(level, message) => {
        timeout.delay()
        map.put("level", level)
        map.put("message", message)
      }
      case exceptionLine: String => {
        timeout.delay()
        map.put("message", map("message") + " \n" + exceptionLine)
      }
    }
  }

  private def millisJava7(dateStr: String): String = {
    dateFormatJava7.parse(dateStr).getTime.toString
  }

  private def millisJava6(dateStr: String): String = {
    dateFormatJava6.parse(dateStr).getTime.toString
  }

  private def nextSequence(): String = {
    sequence += 1
    sequence.toString
  }

  private def indicateNext(map: mutable.Map[String, String]) {
    if (!map.isEmpty) {
      //todo
      throw new UnsupportedOperationException("temporarily broken")
      //dispatch(map)
    }

    // Clean out the map ready for the next record
    map.clear()
  }

}
