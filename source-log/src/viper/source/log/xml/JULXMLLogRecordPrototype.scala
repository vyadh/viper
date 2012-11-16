package viper.source.log.xml

import viper.domain.{Record, RecordField, RecordPrototype}

object JULXMLLogRecordPrototype extends RecordPrototype {

  private val time = new RecordField("Time", convert(_).datetime)

  def fields = Array(
    time,
    new RecordField("Sequence", convert(_).sequence),
    new RecordField("Level", convert(_).level),
    new RecordField("Message", convert(_).message)
  )

  def defaultSort = (time, true)

  private def convert(record: Record): JULXMLLogRecord = {
    try {
      record.asInstanceOf[JULXMLLogRecord]
    } catch {
      case _: ClassCastException =>
        throw new IllegalArgumentException("Unsupported type: " + record)
    }
  }

}
