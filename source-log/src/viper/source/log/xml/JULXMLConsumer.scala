package viper.source.log.xml

import java.io.Reader
import viper.util._
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
   <exception>
     <message>java.lang.RuntimeException: Exception</message>
     <frame>
       <class>com.example.ExampleClass</class>
       <method>exampleMethod</method>
       <line>66</line>
     </frame>
   </exception>
  </record>
  */

  /** XML nodes were are interested in knowing about. */
  private val interesting = Set(
    "record", "millis", "sequence", "level", "message",
    "exception", "frame", "class", "method", "line")

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

    processAllRecords(nodeReader)
  }

  private def processAllRecords(reader: XMLNodeReader) {
    // Temporary storage for node values, needs to be cleared after each record
    val map = new mutable.HashMap[String, String]()

    reader.consume(consumer(map, reader))
  }

  private def consumer(map: mutable.Map[String, String], reader: XMLNodeReader)(node: XMLNode) {
    node match {
      case EndXMLNode("record", _)   => dispatch(map); map.clear()
      case EndXMLNode(name, content) => map(name) = content
      case StartXMLNode("exception") => map("exception") = map.get("exception").getOrElse("") + consumeException(reader)
      case _ =>
    }
  }

  private def consumeException(nodeReader: XMLNodeReader): String = {
    val res = new mutable.StringBuilder(1000)

    def condition(node: XMLNode) = node match {
      case EndXMLNode("exception", _) => false
      case _ => true
    }

    nodeReader.consumeWhile(condition, _ match {
      case EndXMLNode("message", content) => res ++= content += '\n'
      case StartXMLNode("frame")          => res ++= "  at "
      case EndXMLNode("class", content)   => res ++= content
      case EndXMLNode("method", content)  => res += '.' ++= content
      case EndXMLNode("line", content)    => res ++= "  (line " ++= content += ')'
      case EndXMLNode("frame", _)         => res += '\n'
      case _ =>
    })

    res.toString
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
      val message = map.get("message")
      val exception = map.get("exception")

      val body = List(message, exception).flatten.mkString("\n\n")

      // Since XML log records are not persisted, it's not too bad that it's not completely unique
      // (sequence numbers are only unique within a JVM, but we might be opening log files from many)
      val id = sequence + "_" + millis

      new JULXMLLogRecord(id, millis, sequence, level, severity, body)
    } catch {
      case e: NoSuchElementException => throw new Exception(e)//todo handle or let fall through?
    }
  }

}
