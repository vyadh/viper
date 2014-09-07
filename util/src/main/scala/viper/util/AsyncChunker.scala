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
package viper.util

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import scala.collection.mutable
import scala.collection.JavaConversions._

/**
 * A utility that uses a thread to progress single events, and another thread
 * to report them in batches.
 */
class AsyncChunker[T](name: String, periodMillis: Int, process: () => Option[T], deliver: Seq[T] => Unit) {

  private val running = new AtomicBoolean(true)
  private val threads = new mutable.ArrayBuffer[Thread]()
  private val buffer = new LinkedBlockingQueue[T]()

  def start() {
    val processing = new ProcessingThread(buffer)
    val delivery = new DeliveryThread(buffer, deliver)

    threads ++= Seq(processing, delivery)

    processing.start()
    delivery.start()
  }

  def stop() {
    running.set(false)
    threads.foreach(_.join())
    buffer.clear()
  }

  class ProcessingThread(buffer: BlockingQueue[T])
    extends Thread(name + "-processing") {

    override def run() {
      while (running.get) {
        process().foreach(buffer.put(_))
      }
    }
  }

  class DeliveryThread(buffer: BlockingQueue[T], notify: Seq[T] => Unit)
    extends Thread(name + "-delivery") {

    override def run() {
      while (running.get) {
        val records = new java.util.ArrayList[T](500)
        // Obtain all records currently read, and deliver them as a batch
        buffer.drainTo(records)
        if (!records.isEmpty) {
          notify(records.toSeq)
        }
        Thread.sleep(periodMillis)
      }
    }
  }

}
