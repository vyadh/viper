package viper.source.log.jul.plain

import viper.domain.{Record, Subscription, Subscriber}
import viper.source.log.jul.JULLogRecordPrototype
import viper.util.PersistentFileReader
import viper.source.log.regular.JULSimpleConsumer

class JULSimpleLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULLogRecordPrototype) {

  private val reader = new PersistentFileReader(subscriber.query)

  def deliver(to: (Seq[Record]) => Unit) {
    val consumer = new JULSimpleConsumer(reader, record => to(Seq(record)))
    val thread = new JULConsumerThread(consumer)
    thread.start()
  }

  def stop() {
    reader.close()
  }

  class JULConsumerThread(consumer: => JULSimpleConsumer) extends Thread {
    override def run() {
      consumer.consume()
    }
  }

}
