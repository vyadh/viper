package viper

import domain.Subscriber
import source.log.fake.FakeLogSource
import source.log.jms.JMSLogSource
import source.log.random.RandomLogSource
import source.log.xml.JULXMLLogSource
import source.{CompositeSource, Source}
import ui.ViperFrame

object Viper {

  def main(args: Array[String]) {
    val success = sendToRunningInstance(args)
    if (!success) {
      val frame = initUI()
      val sources = allSources
      loadSubscribers(sources, frame)
      loadFiles(args)
      startServer(sources, frame)
    }
  }

  /** If there are log files to open, check whether there is a running instance we can use first. */
  def sendToRunningInstance(files: Array[String]): Boolean = {
    time("Send") {
      if (files.isEmpty) return false
      ViperClient.send(files)
    }
  }

  def startServer(source: Source, frame: ViperFrame) {
    def addSubscriber(subscriber: Subscriber) {
      val subcription = source.subscribe(subscriber)
      frame.addSubscription(subcription)
      frame.toFront()
    }

    val server = new ViperServer
    server.start(addSubscriber)
    // todo subscribe to frame closing?
    server
  }

  def initUI(): ViperFrame = {
    time("UI") {
      val frame = new ViperFrame("Viper")
      frame.setVisible()
      frame
    }
  }

  def loadSubscribers(source: Source, frame: ViperFrame) {
    val config = configFromFile("viper-subscribers.xml")

    for (subscriber <- config.load()) {
      val subscription = source.subscribe(subscriber)
      frame.addSubscription(subscription)
    }
  }

  def loadFiles(files: Array[String]) {
    //todo
  }

  def configFromFile(file: String): SubscriberConfig = {
    val config = new SubscriberConfig(file)
    // todo Hard code some development subscribers for now
    config.add(new Subscriber("fake", "Fake", ""))
    config.add(new Subscriber("random", "Random", ""))
    config.add(new Subscriber("jul-xml", "mrd.log", "K:\\mrd\\logs\\mrd_0.log"))
    config
  }

  def allSources: Source = {
    new CompositeSource(Seq(
      new FakeLogSource,
      new RandomLogSource,
      new JMSLogSource,
      new JULXMLLogSource
    ))
  }

  def time[T](id: String)(block: => T): T = {
    val start = System.currentTimeMillis
    val result = block
    val end = System.currentTimeMillis
    println(id + ": " + (end-start))
    result
  }

}
