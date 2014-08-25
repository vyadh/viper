package viper.source.log.jul

import viper.domain._
import collection.mutable

abstract class AbstractJULConsumer(notify: Record => Unit) {

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

  protected def dispatch(map: mutable.Map[String, String]) {
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
