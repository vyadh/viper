package viper.ui

import viper.domain.Subscriber
import ca.odell.glazedlists.calculation.Calculation

case class Subscribed(subscriber: Subscriber, unread: Calculation[Integer])
