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
package viper.source.log.autofile

import viper.source.Source
import viper.domain.{Subscription, Subscriber}
import viper.source.log.jul.xml.JULXMLLogSubscription
import viper.source.log.jul.plain.JULSimpleLogSubscription

/** Detect file type, and dispatch to appropriate underlying source. */
class AutoFileSource extends Source {

  private val simpleRegexJava7 = """([A-Z][a-z]{2} \d\d, \d{4} \d{1,2}:\d\d:\d\d [AP]M) .+"""
  private val simpleRegexJava6 = """(\d\d-[A-Z][a-z]{2}-\d{4} \d\d:\d\d:\d\d) .+"""
  private val xmlIndicators  = List("<log>", "<record>")

  /*
04-Apr-2013 09:31:43 sun.rmi.server.UnicastRef invoke
FINE: pool-20-thread-1: free connection (reuse = true)
   */

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
    if (xmlIndicators.forall(chunk.contains(_))) {
      return XMLJUL
    }
    val line = firstLine(chunk)
    if (line.matches(simpleRegexJava7) || line.matches(simpleRegexJava6)) {
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
