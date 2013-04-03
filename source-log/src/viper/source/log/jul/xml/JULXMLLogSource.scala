package viper.source.log.xml

import viper.source.log.LogSource
import viper.domain.Subscriber
import viper.source.log.jul.xml.JULXMLLogSubscription

class JULXMLLogSource extends LogSource {

  def supports(subscriber: Subscriber) = subscriber.ref == "jul-xml"

  def subscribe(subscriber: Subscriber) = new JULXMLLogSubscription(subscriber)

}
