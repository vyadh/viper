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
package viper.source.log.fake

import viper.domain._
import viper.domain.log.{LogRecord, LogRecordPrototype}
import scala.Array
import java.util.Date
import viper.domain.Subscriber
import viper.domain.Subscription
import viper.source.log.LogSource

class FakeLogSource extends LogSource {

  def supports(subscriber: Subscriber) = subscriber.ref == "fake"

  def subscribe(subscriber: Subscriber) = new Subscription(subscriber, LogRecordPrototype) {
    def deliver(to: (Seq[Record]) => Unit) {
      to(testData)
    }

    def stop() {}
  }

  private val longLine = "One very long line. " * 10
  private val manyLines = ("Many lines. " * 10).split("\\.").mkString("\n")
  private val manyLongLines = ((("Many long lines. " * 10) + "|") * 10).split("\\|").mkString("\n")

  private lazy val testData = Array(
    new LogRecord("1", "xp05", new Date(), Warning, "CTS Rates", "No rates", false),
    new LogRecord("2", "xp05", new Date(), Warning, "CTS Compliance", "Banks have crashed", true),
    new LogRecord("3", "xp03", new Date(), Info, "MRD", "MRD started successfully", false),
    new LogRecord("4", "xp12", new Date(), Info, "Grid Node", "Grid Node started successfully", false),
    new LogRecord("5", "xp01", new Date(), Severe, "Trades Rec", "Stuff Portia", false),
    new LogRecord("6", "xp01", new Date(), Severe, "Portia", "I'm dead", false),
    new LogRecord("7", "xp08", new Date(), Info, "Portia", "Test commit to BB", true),
    new LogRecord("8", "xp08", new Date(), Config, "MRD", "Some param", false),
    new LogRecord("9", "xp08", new Date(), Fine, "Whoo", manyLongLines, false),
    new LogRecord("10", "xp08", new Date(), Finer, "whooo", manyLines, false),
    new LogRecord("11", "xp08", new Date(), Finest, "Whooo", longLine, false)
  )

}
