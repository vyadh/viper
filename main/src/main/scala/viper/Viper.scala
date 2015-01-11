/*
 * Copyright 2012-2014 Kieron Wilkinson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package viper

import domain.Subscriber
import source.log.autofile.AutoFileSource
import source.log.fake.FakeLogSource
import source.log.jms.JMSLogSource
import source.log.random.RandomLogSource
import source.log.regular.JULSimpleLogSource
import source.log.xml.JULXMLLogSource
import source.{CompositeSource, Source}
import viper.ui.{ViperFX, ViperFrame, DragAndDropHandler, ViperUI}
import java.io.File

object Viper {

  val uiType = "swing";

  def main(args: Array[String]) {
    val success = sendToRunningInstance(args)
    if (!success) {
      val ui = initUI()
      val sources = allSources
      loadSubscribers(sources, ui)
      loadFiles(sources, args, ui)
      startDragAndDrop(sources, ui)
      startServer(sources, ui)
    }
  }

  /** If there are log files to open, check whether there is a running instance we can use first. */
  def sendToRunningInstance(files: Array[String]): Boolean = {
    time("Send") {
      if (files.isEmpty) return false
      ViperClient.send(files)
    }
  }

  def startDragAndDrop(source: Source, ui: ViperUI) {
    ui match {
      case frame: ViperFrame => DragAndDropHandler.install(
        frame, path => addAutoFileSubscriber(path, source, frame))
      case _ =>
    }
  }

  def startServer(source: Source, ui: ViperUI) {
    val server = new ViperServer
    server.start(path => addAutoFileSubscriber(new File(path), source, ui))
  }
  
  def addAutoFileSubscriber(file: File, source: Source, frame: ViperUI) {
    addSubscriber(autoFileSubscriber(file), source, frame)
  }

  def addSubscriber(subscriber: Subscriber, source: Source, ui: ViperUI) {
    if (!ui.hasSubscriber(subscriber)) {
      val subscription = source.subscribe(subscriber)
      ui.addSubscription(subscription)
    }
    ui.toFront()
    ui.focusOn(subscriber)
  }

  def initUI(): ViperUI = {
    time("UI") {
      uiType match {
        case "swing" =>
          val frame = new ViperFrame("Viper")
          frame.setVisible()
          frame
        case "javafx" =>
          ViperFX.launch()
      }
    }
  }

  def loadSubscribers(source: Source, ui: ViperUI) {
    val config = configFromFile("viper-subscribers.xml")

    for (subscriber <- config.load()) {
      val subscription = source.subscribe(subscriber)
      ui.addSubscription(subscription)
    }
  }

  def loadFiles(source: Source, files: Array[String], ui: ViperUI) {
    for (path <- files) {
      val subscriber = autoFileSubscriber(new File(path))
      val subscription = source.subscribe(subscriber)
      ui.addSubscription(subscription)
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
