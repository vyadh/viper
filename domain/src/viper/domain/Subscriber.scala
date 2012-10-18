package viper.domain

/**
 * A reference to a subscription.
 */
case class Subscriber(
      name: String,
      ref: String,
      query: String = "")
