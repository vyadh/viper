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
