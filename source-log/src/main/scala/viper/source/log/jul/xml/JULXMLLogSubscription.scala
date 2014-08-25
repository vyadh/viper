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
