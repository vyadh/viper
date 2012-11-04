package viper

import domain.Subscriber

class SubscriberConfig(file: String) {

  private var subscribers = Set[Subscriber]()

  def load(): Set[Subscriber] = {
    subscribers
  }

  def save() = {
  }

  def add(subscriber: Subscriber) {
    subscribers = subscribers + subscriber
  }

  def remove(subscriber: Subscriber) {
    subscribers = subscribers - subscriber
  }

}
