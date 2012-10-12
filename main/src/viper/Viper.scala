package viper

import domain.log._
import domain.{Record, Subscriber, Subscription}
import ui.ViperFrame
import java.awt.EventQueue
import java.util.Date

object Viper {

  def main(args: Array[String]) {
    val frame = new ViperFrame("Viper")

    EventQueue.invokeLater(new Runnable {
      def run() {
        frame.setVisible(true)
      }
    })

    frame.addSubscription(fake)
    frame.addSubscription(random)
  }

  def fakeSubscriber = new Subscriber("Fake", "Fake Ref", "")
  def fake = new Subscription(fakeSubscriber, LogRecordPrototype) {
    def testData = Array(
      new LogRecord("1", "xp05", new Date(), Warning, "CTS Rates", "No rates"),
      new LogRecord("2", "xp05", new Date(), Warning, "CTS Compliance", "Banks have crashed"),
      new LogRecord("3", "xp03", new Date(), Info, "MRD", "MRD started successfully"),
      new LogRecord("4", "xp12", new Date(), Info, "Grid Node", "Grid Node started successfully"),
      new LogRecord("5", "xp01", new Date(), Error, "Trades Rec", "Stuff Portia"),
      new LogRecord("6", "xp01", new Date(), Error, "Portia", "I'm dead")
    )
  }

  def randomSubscriber = new Subscriber("Random", "Random Ref", "")
  def random = new Subscription(randomSubscriber, LogRecordPrototype) {
    def testData = (1 to 100000).map(randomRecord).toArray
  }
  def randomRecord: (Int => Record) = i => {
    new LogRecord("" + i, "source " + i, new Date(), Warning, "app " + i, "" + i)
  }

}
