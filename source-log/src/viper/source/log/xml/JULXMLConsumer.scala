package viper.source.log.xml

import java.io.Reader
import viper.util.{StripXMLFilterReader, XMLNode, XMLNodeReader}
import collection.mutable
import viper.domain._

class JULXMLConsumer(reader: => Reader, notify: Record => Unit) {

  /*
  <record>
   <date>2000-08-23 19:21:05</date>
   <millis>967083665789</millis>
   <sequence>1256</sequence>
   <logger>kgh.test.fred</logger>
   <level>INFO</level>
   <class>kgh.test.XMLTest</class>
   <method>writeLog</method>
   <thread>10</thread>
   <message>Hello world!</message>
  </record>
  */

  /** XML nodes were are interested in knowing about. */
  private val interesting = Set("record", "millis", "sequence", "level", "message")

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

  def consume() {
    val filteredReader = new StripXMLFilterReader(reader)
    val nodeReader = new XMLNodeReader(filteredReader, interesting)

    val item = new mutable.HashMap[String, String]()

    var next: Option[XMLNode] = None
    do {
      next = nodeReader.next()
      if (next.isDefined) {
        process(next.get, item)
      }
    } while (next != None)
  }

  private def process(node: XMLNode, item: mutable.Map[String, String]) {
    node.name match {
      case "record" => dispatch(item)
      case _ => item(node.name) = node.content
    }
  }

  private def dispatch(map: mutable.Map[String, String]) {
    val record = parse(map)
    notify(record)
  }

  private def parse(map: mutable.Map[String, String]): JULXMLLogRecord = {
    try {
      val millis = map("millis").toLong
      val sequence = map("sequence").toInt
      val level = map("level")
      val severity = severities(level)
      val message = map("message")

      // Since XML log records are not persisted, it's not too bad that it's not completely unique
      // (sequence numbers are only unique within a JVM, but we might be opening log files from many)
      val id = sequence + "_" + millis

      new JULXMLLogRecord(id, millis, sequence, level, severity, message)
    } catch {
      case e: NoSuchElementException => throw new Exception(e)//todo handle or let fall through?
    }
  }

}
