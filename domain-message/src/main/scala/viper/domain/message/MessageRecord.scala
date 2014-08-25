package viper.domain.message

import viper.domain.{Info, Movable, Record, Readable}

case class MessageRecord(
      id: String,
      replyQ: String,
      body: String,
      var read: Boolean) extends Record with Movable with Readable {

  val severity = Info

}
