package viper.source.log.autofile

import viper.source.Source
import viper.domain.{Subscription, Subscriber}
import viper.source.log.jul.xml.JULXMLLogSubscription
import viper.source.log.jul.plain.JULSimpleLogSubscription

/** Detect file type, and dispatch to appropriate underlying source. */
class AutoFileSource extends Source {

  private val simpleRegex = """([A-Z][a-z]{2} \d\d, \d{4} \d{1,2}:\d\d:\d\d [AP]M) .+"""
  private val xmlContent  = """<!DOCTYPE log SYSTEM "logger.dtd">"""

  def supports(subscriber: Subscriber) = subscriber.ref == "auto-file"

  def subscribe(subscriber: Subscriber): Subscription = {
    def withName(name: String) = new Subscriber(name, subscriber.name, subscriber.query)

    detect(subscriber.query) match {
      case XMLJUL    => new JULXMLLogSubscription(withName("jul-xml"))
      case SimpleJUL => new JULSimpleLogSubscription(withName("jul-simple"))
    }
  }

  def detect(path: String): AnyRef = {
    val chunk = readChunk(path)
    if (chunk.contains(xmlContent)) {
      return XMLJUL
    }
    if (firstLine(chunk).matches(simpleRegex)) {
      return SimpleJUL
    }
    throw new UnsupportedOperationException("Unknown file type: " + path)
  }

  def readChunk(path: String): String = {
    scala.io.Source.fromFile(path).getLines().take(5).mkString("\n")
  }

  def firstLine(text: String): String = {
    val line = text.takeWhile(_ != '\n')
    line
  }


  sealed class JULType
  case object SimpleJUL extends JULType
  case object XMLJUL extends JULType

}