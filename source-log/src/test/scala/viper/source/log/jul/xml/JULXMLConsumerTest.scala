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
package viper.source.log.jul.xml

import java.io.StringReader
import java.util.Date

import org.scalatest.{Matchers, FunSuite}
import viper.source.log.jul.JULLogRecord
import viper.source.log.xml.JULXMLConsumer

/**
 * Created by kieron on 06/09/2014.
 */
class JULXMLConsumerTest extends FunSuite with Matchers {

  test("empty stream will return none") {
    val xml = " <log>  </log>  "

    val reader = new StringReader(xml)
    val consumer = new JULXMLConsumer(reader)

    for (_ <- 1 to 10) {
      consumer.next() should equal (None)
    }
  }

  test("stream with full XML record returns right details") {
    val xml =
      """
        |<record>
        |  <date>2014-09-06T13:43:22</date>
        |  <millis>1410007402801</millis>
        |  <sequence>161651</sequence>
        |  <logger>generated</logger>
        |  <level>INFO</level>
        |  <class>viper.util.LogFileGenerator</class>
        |  <method>logRandom</method>
        |  <thread>1</thread>
        |  <message>Now calculating the number of atoms in the universe</message>
        |</record>
      """.stripMargin

    val reader = new StringReader(xml)
    val consumer = new JULXMLConsumer(reader)

    val record = consumer.nextExpected().asInstanceOf[JULLogRecord]
    record.id should equal ("161651_1410007402801")
    record.datetime should equal (new Date(1410007402801L))
    record.timestamp should equal (1410007402801L)
    record.level should equal ("INFO")
    record.message should equal ("Now calculating the number of atoms in the universe")
  }

}
