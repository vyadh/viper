/*
 * Copyright 2012-2014 Kieron Wilkinson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package viper.source.log.xml

import java.io.Reader
import viper.util._
import collection.mutable
import viper.domain._
import viper.source.log.jul.{JULConsumer, AbstractJULConsumer}

class JULXMLConsumer(reader: => Reader) extends AbstractJULConsumer with JULConsumer {

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

  /** Temporary storage for node values, needs to be cleared after each record. */
  lazy val map = new mutable.HashMap[String, String]()
  /** Strip characters that would make XML document invalid on file rolling. */
  lazy val filteredReader = new StripXMLFilterReader(reader)
  /** Reading nodes from the stream. */
  lazy val nodeReader = new XMLNodeReader(filteredReader, interesting)

  def nextExpected(): Record = next() match {
    case Some(x) => x
    case None => nextExpected()
  }

  /**
   * Read the next available record.
   * @return the next record, or none if no records are available or ready
   */
  def next(): Option[Record] = {
    var record: Option[Record] = None
    do {
      record = nodeReader.next().flatMap(consume(_))
    } while (record.isEmpty && isProcessingRecord)

    record
  }

  private def isProcessingRecord = !map.isEmpty

  private def consume(node: XMLNode): Option[Record] = {
    node match {
      case EndXMLNode("record", _)   => val record = parse(map); map.clear(); Some(record)
      case EndXMLNode(name, content) => map(name) = content; None
      case StartXMLNode("exception") => map("exception") = accumulateException(); None
      case _ => None
    }
  }

  private def accumulateException(): String = {
    map.get("exception").getOrElse("") + consumeException(nodeReader)
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
