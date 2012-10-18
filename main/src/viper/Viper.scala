package viper

import domain._
import domain.log._
import domain.{Record, Subscriber, Subscription}
import store.log.xml.{JULXMLConsumer, JULXMLLogRecordPrototype}
import ui.ViperFrame
import java.util.Date
import util.{EQ, PersistentFileReader}

object Viper {

  def main(args: Array[String]) {
    val frame = new ViperFrame("Viper")

    frame.setVisible()

    frame.addSubscription(fake)
    frame.addSubscription(random)
    frame.addSubscription(jul)
  }

  val name = "file.log"
  val path = "/path/to/file" + name
  def julSubscriber = new Subscriber(name, path)
  def jul = new Subscription(julSubscriber, JULXMLLogRecordPrototype) {
    // todo needs to be done via Store
    val pr = new PersistentFileReader(path)
    var c: JULXMLConsumer = null
    val thread = new Thread(new Runnable() {
      def run() {
        c.consume()
      }
    })
    def deliver(to: (Seq[Record]) => Unit) {
      c = new JULXMLConsumer(pr, r => to(Seq(r)))
      thread.start()
    }
    def stop() {
      pr.close()
    }
  }

  def fakeSubscriber = new Subscriber("Fake", "Fake Ref", "")
  def fake = new Subscription(fakeSubscriber, LogRecordPrototype) {
    def deliver(to: (Seq[Record]) => Unit) {
      to(testData)
    }

    def stop() {}

    private lazy val testData = Array(
      new LogRecord("1", "xp05", new Date(), Warning, "CTS Rates", "No rates"),
      new LogRecord("2", "xp05", new Date(), Warning, "CTS Compliance", "Banks have crashed"),
      new LogRecord("3", "xp03", new Date(), Info, "MRD", "MRD started successfully"),
      new LogRecord("4", "xp12", new Date(), Info, "Grid Node", "Grid Node started successfully"),
      new LogRecord("5", "xp01", new Date(), Severe, "Trades Rec", "Stuff Portia"),
      new LogRecord("6", "xp01", new Date(), Severe, "Portia", "I'm dead"),
      new LogRecord("7", "xp08", new Date(), Info, "Portia", "Test commit to BB")
    )
  }

  def randomSubscriber = new Subscriber("Random", "Random Ref", "")
  def random = new Subscription(randomSubscriber, LogRecordPrototype) {
    def deliver(to: (Seq[Record]) => Unit) {
      to((1 to 100000).map(randomRecord))
    }

    def stop() {}
  }

  def randomRecord: (Int => Record) = i => {
    new LogRecord("" + i, "source " + i, new Date(), Warning, "app " + i, "" + i)
  }

}
