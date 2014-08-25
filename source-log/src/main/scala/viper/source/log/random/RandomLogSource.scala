package viper.source.log.random

import viper.domain.{Subscription, Subscriber, Severities, Record}
import viper.domain.log.{LogRecordPrototype, LogRecord}
import viper.source.log.LogSource
import java.util.Date

class RandomLogSource extends LogSource {

  private val rand = new java.util.Random

  def supports(subscriber: Subscriber) = subscriber.ref == "random"

  def subscribe(subscriber: Subscriber) = new Subscription(subscriber, LogRecordPrototype) {
    def deliver(to: (Seq[Record]) => Unit) {
      to((1 to 100000).map(randomRecord))
    }

    def stop() {}
  }

  private def randomRecord: (Int => Record) = i => {
    val s = Severities.values(rand.nextInt(Severities.count))
    new LogRecord("" + i, "source " + i, new Date(), s, "app " + i, "" + i, false)
  }

}
