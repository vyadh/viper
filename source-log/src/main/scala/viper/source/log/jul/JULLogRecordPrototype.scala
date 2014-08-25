package viper.source.log.jul

import viper.domain.{Record, RecordField, RecordPrototype}

object JULLogRecordPrototype extends RecordPrototype {

  private val time = new RecordField("Time", convert(_).datetime)
  private val sequence = new RecordField("Seq", convert(_).sequence)

  def fields = Array(
    time,
    sequence,
    new RecordField("Level", convert(_).level),
    new RecordField("Message", convert(_).message)
  )

  def defaultSort = List(
    (time, true),
    (sequence, true)
  )

  private def convert(record: Record): JULLogRecord = {
    try {
      record.asInstanceOf[JULLogRecord]
    } catch {
      case _: ClassCastException =>
        throw new IllegalArgumentException("Unsupported type: " + record)
    }
  }

}
