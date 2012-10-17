package viper.store.log.xml

import java.io.Reader
import javax.xml.stream.XMLInputFactory

class JULXMLConsumer(reader: Reader, notify: LogItem => Unit) {

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

  def consume() {
    val eventReader = xmlEventReader(reader)
    //
  }

  def xmlEventReader(reader: Reader) =
    XMLInputFactory.newInstance.createXMLEventReader(reader)

}

case class LogItem(timestamp: Long, level: String, message: String)
