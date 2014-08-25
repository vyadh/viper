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

import viper.domain.{Record, Subscription, Subscriber}
import viper.source.log.jul.JULLogRecordPrototype
import viper.util.PersistentFileReader
import viper.source.log.xml.JULXMLConsumer

class JULXMLLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULLogRecordPrototype) {

  private val reader = new PersistentFileReader(subscriber.query)

  def deliver(to: (Seq[Record]) => Unit) {
    val consumer = new JULXMLConsumer(reader, record => to(Seq(record)))
    val thread = new JULXMLConsumerThread(consumer)
    thread.start()
  }

  def stop() {
    reader.close()
  }

  class JULXMLConsumerThread(consumer: => JULXMLConsumer) extends Thread {
    override def run() {
      consumer.consume()
    }
  }

}
