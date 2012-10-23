package viper.util

import java.io.Reader
import javax.xml.stream._
import scala.Some
import XMLStreamConstants._

class XMLNodeReader(reader: Reader, interesting: String => Boolean) {

  var sr = createXMLStreamReader(reader)

  /**
   * @return xml nodes until there are no more, in which case return none
   */
  def next(): Option[XMLNode] = {
    var name = ""
    var reading = false
    val content = new StringBuilder

    while (sr.hasNext) {

      sr.next() match {
        case START_ELEMENT =>
          name = sr.getLocalName
          reading = interesting(name)
          content.clear()

        case CHARACTERS if reading =>
          content.append(sr.getText)

        case END_ELEMENT =>
          name = sr.getLocalName
          if (interesting(name)) {
            val node = new XMLNode(name, content.toString)
            return Some(node)
          }

        case END_DOCUMENT =>
          reader.close()
          sr.close()

        case _ =>
      }
    }

    None
  }

  def createXMLStreamReader(reader: Reader): XMLStreamReader = {
    XMLInputFactory.newInstance.createXMLStreamReader(reader)
  }

}

case class XMLNode(name: String, content: String)
