package viper.domain

abstract case class Subscription(
      subscriber: Subscriber,
      prototype: RecordPrototype) {

  def ref = subscriber.ref
  def name = subscriber.name

  /**
   * Adds a new delivery point for messages.
   * @param to where to deliver messages.
   */
  def deliver(to: Seq[Record] => Unit)

  /**
   * Close the subscription, so no more events are delivered.
   * Duplicate calls have no effect.
   */
  def stop()

}
