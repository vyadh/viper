package viper.util

import java.io.Reader
import javax.xml.stream.{XMLStreamConstants, XMLInputFactory}

class XMLNodeReader(reader: Reader, interesting: String => Boolean) {

  lazy val er = XMLInputFactory.newInstance.createXMLEventReader(reader)

  /**
   * @return xml nodes until there are no more, in which case return none
   */
  def next(): Option[XMLNode] = {
    var name = ""
    var reading = false
    val content = new StringBuilder

    while (er.hasNext) {
      val event = er.nextEvent()

      event.getEventType match {
        case XMLStreamConstants.START_ELEMENT =>
          name = event.asStartElement.getName.getLocalPart
          reading = interesting(name)
          content.clear()

        case XMLStreamConstants.CHARACTERS if reading =>
          content.append(event.asCharacters().getData)

        case XMLStreamConstants.END_ELEMENT =>
          name = event.asEndElement.getName.getLocalPart
          if (interesting(name)) {
            val node = new XMLNode(name, content.toString)
            return Some(node)
          }

        case _ =>
      }
    }
    None
  }

}

case class XMLNode(name: String, content: String)
