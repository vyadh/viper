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

import java.util
import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicBoolean

import viper.domain.{Record, Subscription, Subscriber}
import viper.source.log.jul.JULLogRecordPrototype
import viper.source.log.xml.JULXMLConsumer
import viper.util.PersistentFileReader

import scala.collection.mutable
import scala.collection.JavaConversions._

class JULXMLLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULLogRecordPrototype) {

  private val running = new AtomicBoolean(true)
  private val reader = new PersistentFileReader(subscriber.query)
  private val threads = new mutable.ArrayBuffer[Thread]()

  def deliver(notify: Seq[Record] => Unit) {
    val buffer = new LinkedBlockingQueue[Record]()
    val processing = new ProcessingThread(buffer)
    val delivery = new DeliveryThread(buffer, notify)

    threads ++= Seq(processing, delivery)

    processing.start()
    delivery.start()
  }

  def stop() {
    running.set(false)
    reader.close()
    threads.foreach(_.join())
  }

  class ProcessingThread(buffer: BlockingQueue[Record])
        extends Thread(subscriber.ref + "-processing") {

    override def run() {
      val consumer = new JULXMLConsumer(reader)
      while (running.get) {
        consumer.next().foreach(buffer.put(_))
      }
    }
  }

  class DeliveryThread(buffer: BlockingQueue[Record], notify: Seq[Record] => Unit)
        extends Thread(subscriber.ref + "-delivery") {

    override def run() {
      while (running.get) {
        val records = new util.ArrayList[Record](100)
        // Obtain all records currently read, and deliver them as a batch
        buffer.drainTo(records)
        if (!records.isEmpty) {
          notify(records.toSeq)
        }
        Thread.sleep(100)
      }
    }
  }

}
