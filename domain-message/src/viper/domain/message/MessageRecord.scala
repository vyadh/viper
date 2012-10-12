package viper.domain.message

import viper.domain.{Movable, Record}

case class MessageRecord(
      id: String,
      replyQ: String,
      body: String) extends Record with Movable
