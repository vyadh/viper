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
package viper.util

import java.io.Reader
import javax.xml.stream._
import com.ctc.wstx.api.WstxInputProperties
import com.ctc.wstx.stax.WstxInputFactory
import XMLStreamConstants._

class XMLNodeReader(reader: Reader, interesting: String => Boolean) {

  val xmlInputFactory = createXMLInputFactory
  val sr = createXMLStreamReader(reader)
  var reading = false

  def consume(consumer: XMLNode => Unit) {
    consumeWhile(_ => true, consumer)
  }

  /**
   * Keeps consuming nodes while the condition is true.
   */
  def consumeWhile(condition: XMLNode => Boolean, consumer: XMLNode => Unit) {
    var node: Option[XMLNode] = None
    do {
      node = next()

      if (node.isDefined) {
        consumer(node.get)
      }
    } while (node != None && condition(node.get))
  }

  /**
   * @return xml nodes until there are no more, in which case return none
   */
  def next(): Option[XMLNode] = {
    val content = new StringBuilder

    while (sr.hasNext) {

      sr.next() match {
        case START_ELEMENT =>
          val name = sr.getLocalName
          reading = interesting(name)
          content.clear()
          if (reading) {
            return Some(new StartXMLNode(name))
          }

        case CHARACTERS if reading =>
          content.append(sr.getText)

        case END_ELEMENT =>
          val name = sr.getLocalName
          if (interesting(name)) {
            return Some(new EndXMLNode(name, content.toString))
          }

        case END_DOCUMENT =>
          reader.close()
          sr.close()

        case _ =>
      }
    }

    None
  }

  def createXMLInputFactory: XMLInputFactory = {
    import XMLInputFactory._
    import WstxInputProperties._

    val factory = new WstxInputFactory
    factory.configureForSpeed()
    factory.getConfig.doSupportDTDs(false)
    factory.getConfig.doSupportDTDPP(false)
    factory.setProperty(SUPPORT_DTD, false)
    factory.setProperty(IS_VALIDATING, false)
    factory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false)
    factory.setProperty(P_INPUT_PARSING_MODE, PARSING_MODE_FRAGMENT)
    factory
  }

  def createXMLStreamReader(reader: Reader): XMLStreamReader = {
    xmlInputFactory.createXMLStreamReader(reader)
  }

}

trait XMLNode {
  val name: String
}
case class StartXMLNode(name: String) extends XMLNode
case class EndXMLNode(name: String, content: String) extends XMLNode
