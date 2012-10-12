package viper.domain.log

import viper.domain.{Levelable, Record}
import java.util.Date

/**
 * A log record may or may not be {@link com.pareto.viper.domain.Persistable}, it depends on the
 * Store, which may mix that trait in at runtime. E.g. Log content coming from JMS would be,
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
      body: String) extends Record with Levelable
