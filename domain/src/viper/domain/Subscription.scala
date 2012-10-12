package viper.domain

abstract case class Subscription(
      subscriber: Subscriber,
      prototype: RecordPrototype) {

  def ref = subscriber.ref

  def testData: Array[Record]

}
