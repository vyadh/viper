package viper.source.log.regular

import viper.domain.{Subscriber, Subscription, Record}
import viper.util.PersistentFileReader
import viper.source.log.LogSource
import viper.source.log.jul.JULLogRecordPrototype

class JULSimpleLogSource extends LogSource {

  def supports(subscriber: Subscriber) = subscriber.ref == "jul-simple"

  def subscribe(subscriber: Subscriber) = new JULLogSubscription(subscriber)

  class JULLogSubscription(subscriber: Subscriber) extends Subscription(subscriber, JULLogRecordPrototype) {
    private val reader = new PersistentFileReader(subscriber.query)

    def deliver(to: (Seq[Record]) => Unit) {
      val consumer = new JULSimpleConsumer(reader, record => to(Seq(record)))
      val thread = new JULConsumerThread(consumer)
      thread.start()
    }

    def stop() {
      reader.close()
    }
  }

  class JULConsumerThread(consumer: => JULSimpleConsumer) extends Thread {
    override def run() {
      consumer.consume()
    }
  }

}
