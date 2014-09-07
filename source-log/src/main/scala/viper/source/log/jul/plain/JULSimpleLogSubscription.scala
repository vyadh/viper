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

import viper.domain.{Record, Subscription, Subscriber}
import viper.source.log.jul.JULLogRecordPrototype
import viper.util.PersistentFileReader
import viper.source.log.regular.JULSimpleConsumer

class JULSimpleLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULLogRecordPrototype) {

  private val reader = new PersistentFileReader(subscriber.query)
  private val consumer = new JULSimpleConsumer(reader)

  def deliver(notify: (Seq[Record]) => Unit) {
    val thread = new JULConsumerThread(consumer, notify)
    thread.start()
  }

  def stop() {
    reader.close()
    // todo finish thread
  }

  class JULConsumerThread(consumer: => JULSimpleConsumer, notify: (Seq[Record]) => Unit) extends Thread {
    override def run() {
      while (true) {
        val next = consumer.next()
        notify(next.toSeq)
      }
    }
  }

}
