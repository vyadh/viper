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
package viper.source.log.jul.plain

import java.io.StringReader
import java.util.Date

import org.scalatest.{FunSuite, Matchers}
import viper.source.log.jul.JULLogRecord
import viper.source.log.regular.JULSimpleConsumer

/**
 * Created by kieron on 06/09/2014.
 */
class JULSimpleConsumerTest extends FunSuite with Matchers {

  test("empty stream will return none") {
    val content = ""

    val reader = new StringReader(content)
    val consumer = new JULSimpleConsumer(reader)

    for (_ <- 1 to 10) {
      consumer.next() should equal (None)
    }
  }

  test("stream with half log line returns nothing") {
    val content = "Apr 02, 2013 9:58:34 AM viper.util.LogFileGenerator$ main"

    val reader = new StringReader(content)
    val consumer = new JULSimpleConsumer(reader)

    consumer.next() should equal (None)
  }

  test("stream with full record returns right details") {
    val content =
      """
        |Apr 02, 2013 9:58:34 AM viper.util.LogFileGenerator$ main
        |WARNING: normal message
      """.stripMargin.trim

    val reader = new StringReader(content)
    val consumer = new JULSimpleConsumer(reader)

    val timestamp = consumer.dateFormatJava7.parse("Apr 02, 2013 9:58:34 AM").getTime

    val record = consumer.nextExpected().asInstanceOf[JULLogRecord]
    record.id should equal ("1_" + timestamp)
    record.datetime should equal (new Date(timestamp))
    record.timestamp should equal (timestamp)
    record.level should equal ("WARNING")
    record.message should equal ("normal message")
  }

  test("record with exception is fully consumed") {
    val content =
      """
        |Apr 02, 2013 9:58:34 AM viper.util.LogFileGenerator$ main
        |SEVERE: my message
        |java.lang.IllegalStateException: my exception
        |  at viper.util.LogFileGenerator$.main(LogFileGenerator.scala:17)
        |  at viper.util.LogFileGenerator.main(LogFileGenerator.scala)
        |  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        |  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        |  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        |  at java.lang.reflect.Method.invoke(Method.java:601)
        |  at com.intellij.rt.execution.application.AppMain.main(AppMain.java:120)
        |Caused by: java.lang.Exception: my cause
        |  ... 7 more
      """.stripMargin.trim

    val reader = new StringReader(content)
    val consumer = new JULSimpleConsumer(reader)

    val timestamp = consumer.dateFormatJava7.parse("Apr 02, 2013 9:58:34 AM").getTime

    val record = consumer.nextExpected().asInstanceOf[JULLogRecord]
    record.id should equal ("1_" + timestamp)
    record.datetime should equal (new Date(timestamp))
    record.timestamp should equal (timestamp)
    record.level should include ("SEVERE")
    record.message should include ("my message")
    record.message should include ("... 7 more")
  }

}
