package viper.source.log.xml

import java.io.Reader
import viper.util._
import collection.mutable
import viper.domain._
import viper.source.log.jul.AbstractJULConsumer

class JULXMLConsumer(reader: => Reader, notify: Record => Unit) extends AbstractJULConsumer(notify) {

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

}
