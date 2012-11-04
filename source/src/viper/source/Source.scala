package viper.source

import viper.domain.{Subscription, Subscriber}

trait Source {

  def supports(subscriber: Subscriber): Boolean

  def subscribe(subscriber: Subscriber): Subscription

}
