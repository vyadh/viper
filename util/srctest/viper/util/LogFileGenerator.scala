package viper.util

import java.util.logging.{LogManager, Level, XMLFormatter, Logger}

object LogFileGenerator {

  def main(args: Array[String]) {
    val manager: LogManager = LogManager.getLogManager
    val names = manager.getLoggerNames
    while (names.hasMoreElements) {
      val l = manager.getLogger(names.nextElement())
      l.getHandlers.foreach(_.setFormatter(new XMLFormatter))
    }

    val logger = Logger.getLogger("mylogger")
    logger.log(Level.SEVERE, "my message", new IllegalStateException("my exception", new Exception("my cause")))
  }

}
