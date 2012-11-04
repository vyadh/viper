package viper.source.log.jms

import viper.source.log.LogSource
import viper.domain.{Subscription, Subscriber}

class JMSLogSource extends LogSource {

  def supports(subscriber: Subscriber) = subscriber.ref == "jms"

  def subscribe(subscriber: Subscriber): Subscription = {
    throw new UnsupportedOperationException
  }

}
