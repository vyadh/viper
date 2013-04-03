package viper.source.log.regular

import viper.domain.Subscriber
import viper.source.log.LogSource
import viper.source.log.jul.plain.JULSimpleLogSubscription

class JULSimpleLogSource extends LogSource {

  def supports(subscriber: Subscriber) = subscriber.ref == "jul-simple"

  def subscribe(subscriber: Subscriber) = new JULSimpleLogSubscription(subscriber)

}
