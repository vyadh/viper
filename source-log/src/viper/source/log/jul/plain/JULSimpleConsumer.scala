package viper.source.log.regular

import java.io.{BufferedReader, Reader}
import collection.mutable
import viper.domain._
import viper.source.log.jul.JULLogRecord
import java.text.SimpleDateFormat

class JULSimpleConsumer(reader: => Reader, notify: Record => Unit) {

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

  /** Mapping from JUL level onto Severity (which is the same, but anyway). */
  private val severities = Map(
    "SEVERE" -> Severe,
    "WARNING" -> Warning,
    "INFO" -> Info,
    "CONFIG" -> Config,
    "FINE" -> Fine,
    "FINER" -> Finer,
    "FINEST" -> Finest
  )

  /** E.g. Apr 02, 2013 9:58:34 AM */
  private val DateTimePattern = """([A-Z][a-z]{2} \d\d, \d{4} \d{1,2}:\d\d:\d\d [AP]M) .+""".r
  /** E.g. INFO: message */
  private val LevelMessagePattern = """(FINEST|FINER|FINE|CONFIG|INFO|WARNING|SEVERE): (.+)""".r

  /** E.g. Apr 02, 2013 9:58:34 AM */
  private val dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa")

  /** Implicit sequence value, used to order events with the same time. */
  private var sequence = 0


  def consume() {
    val bufferedReader = new BufferedReader(reader)

    processAllRecords(bufferedReader)
  }

  private def processAllRecords(reader: BufferedReader) {
    // Temporary storage for node values, needs to be cleared after each record
    val map = new mutable.HashMap[String, String]()

    var line: String = null
    do {
      line = reader.readLine()
      consumer(map, line)
    } while (line != null)
  }

  private def consumer(map: mutable.Map[String, String], line: String) {
    line match {
      case DateTimePattern(dateStr) => {
        //todo not good enough, only sends events when next one comes in
        if (!map.isEmpty) {
          dispatch(map)
        }
        map.clear()
        map.put("millis", millis(dateStr))
        map.put("sequence", nextSequence())
      }
      case LevelMessagePattern(level, message) => {
        map.put("level", level)
        map.put("message", message)
      }
      case exceptionLine: String => {
        map.put("message", map("message") + "\n" + exceptionLine)
      }
    }
  }

  private def millis(dateStr: String): String = {
    dateFormat.parse(dateStr).getTime.toString
  }

  private def nextSequence(): String = {
    sequence += 1
    sequence.toString
  }

  private def dispatch(map: mutable.Map[String, String]) {
    val record = parse(map)
    notify(record)
  }

  private def parse(map: mutable.Map[String, String]): JULLogRecord = {
    try {
      val millis = map("millis").toLong
      val sequence = map("sequence").toInt
      val level = map("level")
      val severity = severities(level)
      val message = map.get("message")
      val exception = map.get("exception")

      val body = List(message, exception).flatten.mkString("\n\n")

      // Since XML log records are not persisted, it's not too bad that it's not completely unique
      // (sequence numbers are only unique within a JVM, but we might be opening log files from many)
      val id = sequence + "_" + millis

      new JULLogRecord(id, millis, sequence, level, severity, body)
    } catch {
      case e: NoSuchElementException => throw new Exception(e)//todo handle or let fall through?
    }
  }

}
