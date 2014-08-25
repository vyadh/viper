package viper

import domain.Subscriber
import source.log.autofile.AutoFileSource
import source.log.fake.FakeLogSource
import source.log.jms.JMSLogSource
import source.log.random.RandomLogSource
import source.log.regular.JULSimpleLogSource
import source.log.xml.JULXMLLogSource
import source.{CompositeSource, Source}
import ui.{DragAndDropHandler, ViperFrame}
import java.io.File

object Viper {

  def main(args: Array[String]) {
    val success = sendToRunningInstance(args)
    if (!success) {
      val frame = initUI()
      val sources = allSources
      loadSubscribers(sources, frame)
      loadFiles(sources, args, frame)
      startDragAndDrop(sources, frame)
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

  def startDragAndDrop(source: Source, frame: ViperFrame) {
    DragAndDropHandler.install(frame, path => addAutoFileSubscriber(path, source, frame))
  }

  def startServer(source: Source, frame: ViperFrame) {
    val server = new ViperServer
    server.start(path => addAutoFileSubscriber(new File(path), source, frame))
  }
  
  def addAutoFileSubscriber(file: File, source: Source, frame: ViperFrame) {
    addSubscriber(autoFileSubscriber(file), source, frame)
  }

  def addSubscriber(subscriber: Subscriber, source: Source, frame: ViperFrame) {
    if (!frame.hasSubscriber(subscriber)) {
      val subscription = source.subscribe(subscriber)
      frame.addSubscription(subscription)
    }
    frame.toFront()
    frame.focusOn(subscriber)
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
      val subscriber = autoFileSubscriber(new File(path))
      val subscription = source.subscribe(subscriber)
      frame.addSubscription(subscription)
    }
  }

  def configFromFile(file: String): SubscriberConfig = {
    val config = new SubscriberConfig(file)
    // todo Hard code some development subscribers for now
//    config.add(new Subscriber("fake", "Fake", ""))
//    config.add(new Subscriber("random", "Random", ""))
    config
  }

  def allSources: Source = {
    new CompositeSource(Seq(
      new FakeLogSource,
      new RandomLogSource,
      new JMSLogSource,
      new AutoFileSource,
      new JULXMLLogSource,
      new JULSimpleLogSource
    ))
  }

  def autoFileSubscriber(file: File): Subscriber = {
    val path = file.toString
    val name = file.getName
    new Subscriber("auto-file", name, path)
  }

  def time[T](id: String)(block: => T): T = {
    val start = System.currentTimeMillis
    val result = block
    val end = System.currentTimeMillis
    println(id + ": " + (end-start))
    result
  }

}
