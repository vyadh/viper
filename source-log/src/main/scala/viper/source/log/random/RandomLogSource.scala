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
package viper.source.log.random

import viper.domain.{Subscription, Subscriber, Severities, Record}
import viper.domain.log.{LogRecordPrototype, LogRecord}
import viper.source.log.LogSource
import java.util.Date

class RandomLogSource extends LogSource {

  private val rand = new java.util.Random

  def supports(subscriber: Subscriber) = subscriber.ref == "random"

  def subscribe(subscriber: Subscriber) = new Subscription(subscriber, LogRecordPrototype) {
    def deliver(to: (Seq[Record]) => Unit) {
      to((1 to 100000).map(randomRecord))
    }

    def stop() {}
  }

  private def randomRecord: (Int => Record) = i => {
    val s = Severities.values(rand.nextInt(Severities.count))
    new LogRecord("" + i, "source " + i, new Date(), s, "app " + i, "" + i, false)
  }

}
