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
  private[log] val dateFormatJava7 = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa")
  /** E.g. 04-Apr-2013 09:31:43 */
  private[log] val dateFormatJava6 = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss")

  private lazy val buffer = new BufferedReader(reader)
  /** Temporary storage for node values, needs to be cleared after each record. */
  private val map = new mutable.HashMap[String, String]()
  /** Implicit sequence value, used to order events with the same time. */
  private var sequence = 0


  def nextExpected(): Record = next() match {
    case Some(x) => x
    case None => nextExpected()
  }

  /**
   * Read the next available record.
   * @return the next record, or none if no records are available or ready
   */
  def next(): Option[Record] = {
    val line = buffer.readLine()

    // Indicate when we have a completed record at the end of the stream
    if (line == null && isPopulated) {
      return Some(parse(map))
    }

    // We've read something, but we don't quite know if it is the complete record yet
    if (line != null) {
      val read = pull(line)

      // Indicate we have a completed record at the start of the next
      if (isStartOfRecord(read) && isPopulated) {
        val previous = parse(map)
        consume(read)
        return Some(previous)
      }
      // Consume the current line information
      else {
        consume(read)
        // This might be the end, but we can't tell
      }
    }

    None
  }

  private def isStartOfRecord(read: Read) = read.isInstanceOf[TimeHeader]

  private def isPopulated = map.contains("millis") && map.contains("message")

  private def pull(line: String): Read = {
    line match {
      case DateTimePatternJava7(dateStr) => TimeHeader(millisJava7(dateStr))
      case DateTimePatternJava6(dateStr) => TimeHeader(millisJava6(dateStr))
      case LevelMessagePattern(level, message) => LevelMessage(level, message)
      case exceptionLine: String => Message(exceptionLine)
    }
  }

  private def consume(read: Read) {
    read match {
      case TimeHeader(millis) => {
        map.clear()
        map.put("millis", millis)
        map.put("sequence", nextSequence())
      }
      case LevelMessage(level, message) => {
        map.put("level", level)
        map.put("message", message)
      }
      case Message(message) => {
        map.put("message", map.get("message").map(_ + " \n").getOrElse("") + message)
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

  sealed trait Read
  /** When we know we just read the first line of a record. */
  case class TimeHeader(millis: String) extends Read
  /** When we don't know for sure if there is more of the record or not. */
  case class LevelMessage(level: String, message: String) extends Read
  /** When we don't know for sure if there is more of the record or not. */
  case class Message(message: String) extends Read

}
