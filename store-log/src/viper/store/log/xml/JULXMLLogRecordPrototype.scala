package viper.store.log.xml

import viper.domain.{Record, RecordField, RecordPrototype}

object JULXMLLogRecordPrototype extends RecordPrototype {

  def fields = Array(
    new RecordField("Time", convert(_).datetime),
    new RecordField("Sequence", convert(_).sequence),
    new RecordField("Level", convert(_).level),
    new RecordField("Message", convert(_).message)
  )

  def severity(r: Record) = convert(r).severity

  private def convert(record: Record): JULXMLLogRecord = {
    try {
      record.asInstanceOf[JULXMLLogRecord]
    } catch {
      case _: ClassCastException =>
        throw new IllegalArgumentException("Unsupported type: " + record)
    }
  }

}
