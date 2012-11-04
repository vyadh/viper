package viper.source

import viper.domain.{Subscription, Subscriber}

class CompositeSource(sources: Seq[Source]) extends Source {

  def supports(subscriber: Subscriber) = sources.exists(_.supports(subscriber))

  def subscribe(subscriber: Subscriber): Subscription = {
    sources.find(_.supports(subscriber)).map(_.subscribe(subscriber)) match {
      case Some(subscription) => subscription
      case None => throw new IllegalArgumentException("Unsupported subscriber: " + subscriber)
    }
  }

}
