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
package viper.source.log.jul

import java.io.Reader

import viper.domain.{Record, Subscriber, Subscription}
import viper.util.{AsyncChunker, PersistentFileReader}

/**
 * Created by kieron on 07/09/2014.
 */
abstract class AbstractJULLogSubscription(subscriber: Subscriber)
      extends Subscription(subscriber, JULLogRecordPrototype) {

  private var session: Session = null

  def deliver(notify: Seq[Record] => Unit) {
    if (session != null) {
      throw new IllegalStateException("Subscription already started")
    }
    session = new Session(createFileReader(subscriber.query), notify)
    session.chunker.start()
  }
  
  def stop() {
    if (session != null) {
      session.reader.close()
      session.chunker.stop()
    }
  }

  def createFileReader(path: String): PersistentFileReader

  def process(): Option[Record] = session.consumer.next()

  def createConsumer(reader: Reader): JULConsumer

  class Session(val reader: PersistentFileReader, notify: Seq[Record] => Unit) {
    val consumer = createConsumer(reader)
    val chunker = new AsyncChunker[Record](subscriber.ref, 200, process, notify)
  }

}
