package viper.domain.log

import viper.domain.{Record, RecordField, RecordPrototype}

object LogRecordPrototype extends RecordPrototype {

  private val time = new RecordField("Time", (r: Record) => convert(r).time)

  def fields: Array[RecordField] = Array(
    time,
    new RecordField("Source",      (r: Record) => convert(r).source),
    new RecordField("Severity",    (r: Record) => convert(r).severity),
    new RecordField("Application", (r: Record) => convert(r).application),
    new RecordField("Body",        (r: Record) => convert(r).body)
  )

  def defaultSort = (time, true)

  private def convert(record: Record): LogRecord = {
    try {
      record.asInstanceOf[LogRecord]
    } catch {
      case _: ClassCastException =>
        throw new IllegalArgumentException("Unsupported type: " + record)
    }
  }

}
