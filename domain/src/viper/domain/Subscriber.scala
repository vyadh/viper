package viper.domain

/**
 * A reference to a subscription.
 */
case class Subscriber(
      ref: String,
      name: String,
      query: String = "")
