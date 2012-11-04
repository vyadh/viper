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
    val start = System.currentTimeMillis
    val frame = new ViperFrame("Viper")

    frame.setVisible()
    val end = System.currentTimeMillis
    println("Startup: " + (end-start))

    val config = configFromFile("viper-subscribers.xml")
    val source = allSources

    for (subscriber <- config.load()) {
      val subscription = source.subscribe(subscriber)
      frame.addSubscription(subscription)
    }
  }

  def configFromFile(file: String): SubscriberConfig = {
    val config = new SubscriberConfig(file)
    // todo Hard code some development subscribers for now
    config.add(new Subscriber("fake", "Fake", ""))
    config.add(new Subscriber("random", "Random", ""))
    config.add(new Subscriber("jul-xml", "example.log", "./example.log"))
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

}
