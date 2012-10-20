package viper.store.log.xml

import viper.domain.{Severity, Record, Levelable}
import java.util.Date

/**
 * A log record for java.util.logging XML output.
 */
case class JULXMLLogRecord(
      id: String,
      timestamp: Long,
      sequence: Integer,
      level: String,
      severity: Severity,
      message: String) extends Record with Levelable {

  def datetime = new Date(timestamp)
  def body = message

}
