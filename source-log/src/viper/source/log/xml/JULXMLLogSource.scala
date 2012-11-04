package viper.source.log.xml

import viper.source.log.LogSource
import viper.domain.{Record, Subscription, Subscriber}
import viper.util.PersistentFileReader

class JULXMLLogSource extends LogSource {

  def supports(subscriber: Subscriber) = subscriber.ref == "jul-xml"

  def subscribe(subscriber: Subscriber) = new JULXMLLogSubscription(subscriber)

  class JULXMLLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULXMLLogRecordPrototype) {
    private val reader = new PersistentFileReader(subscriber.query)

    def deliver(to: (Seq[Record]) => Unit) {
      val consumer = new JULXMLConsumer(reader, record => to(Seq(record)))
      val thread = new JULXMLConsumerThread(consumer)
      thread.start()
    }

    def stop() {
      reader.close()
    }
  }

  class JULXMLConsumerThread(consumer: => JULXMLConsumer) extends Thread {
    override def run() {
      consumer.consume()
    }
  }

}
