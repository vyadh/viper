package viper.source.log.regular

import java.io.{BufferedReader, Reader}
import collection.mutable
import viper.domain._
import viper.source.log.jul.AbstractJULConsumer
import java.text.SimpleDateFormat
import viper.util.TimeoutTask

class JULSimpleConsumer(reader: => Reader, notify: Record => Unit) extends AbstractJULConsumer(notify) {

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
      case DateTimePattern(dateStr) => {
        timeout.stage()
        map.put("millis", millis(dateStr))
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

  private def millis(dateStr: String): String = {
    dateFormat.parse(dateStr).getTime.toString
  }

  private def nextSequence(): String = {
    sequence += 1
    sequence.toString
  }

  private def indicateNext(map: mutable.Map[String, String]) {
    if (!map.isEmpty) {
      dispatch(map)
    }

    // Clean out the map ready for the next record
    map.clear()
  }

}
