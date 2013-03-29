package viper.util

import java.io.Reader
import javax.xml.stream._
import scala.Some
import XMLStreamConstants._

class XMLNodeReader(reader: Reader, interesting: String => Boolean) {

  val sr = createXMLStreamReader(reader)
  var reading = false

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

  def createXMLStreamReader(reader: Reader): XMLStreamReader = {
    XMLInputFactory.newInstance.createXMLStreamReader(reader)
  }

}

trait XMLNode {
  val name: String
}
case class StartXMLNode(name: String) extends XMLNode
case class EndXMLNode(name: String, content: String) extends XMLNode
