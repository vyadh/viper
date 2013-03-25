package viper

import domain.Subscriber
import source.log.fake.FakeLogSource
import source.log.jms.JMSLogSource
import source.log.random.RandomLogSource
import source.log.xml.JULXMLLogSource
import source.{CompositeSource, Source}
import ui.ViperFrame
import java.io.File

object Viper {

  def main(args: Array[String]) {
    val success = sendToRunningInstance(args)
    if (!success) {
      val frame = initUI()
      val sources = allSources
      loadSubscribers(sources, frame)
      loadFiles(sources, args, frame)
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
      if (!frame.hasSubscriber(subscriber)) {
        val subscription = source.subscribe(subscriber)
        frame.addSubscription(subscription)
        frame.toFront()
      }
      frame.toFront()
      frame.focusOn(subscriber)
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

  def loadFiles(source: Source, files: Array[String], frame: ViperFrame) {
    for (path <- files) {
      val name = new File(path).getName
      val subscriber = new Subscriber("jul-xml", name, path)
      val subscription = source.subscribe(subscriber)
      frame.addSubscription(subscription)
    }
  }

  def configFromFile(file: String): SubscriberConfig = {
    val config = new SubscriberConfig(file)
    // todo Hard code some development subscribers for now
    config.add(new Subscriber("fake", "Fake", ""))
    config.add(new Subscriber("random", "Random", ""))
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
