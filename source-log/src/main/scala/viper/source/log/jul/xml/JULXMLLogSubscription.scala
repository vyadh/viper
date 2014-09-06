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
import viper.source.log.xml.JULXMLConsumer
import viper.util.PersistentFileReader

class JULXMLLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULLogRecordPrototype) {

  private val reader = new PersistentFileReader(subscriber.query)

  def deliver(notify: Seq[Record] => Unit) {
    // todo bunch
    val consumer = new JULXMLConsumer(reader)
    val thread = new JULXMLConsumerThread(consumer, notify)
    thread.start()
    // todo Shut down thread
  }

  def stop() {
    reader.close()
  }

  class JULXMLConsumerThread(consumer: => JULXMLConsumer, notify: Seq[Record] => Unit)
        extends Thread(subscriber.ref) {
    
    override def run() {
      while (true) { //todo
        val record = consumer.next()
        notify(record.toSeq)
        Thread.`yield`()
      }
    }
  }

}
