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
package viper.util

import java.util.logging.Level
import java.util.logging.Level._

import scala.util.Random

/**
 * Generation of log record..
 */
class LogRecordGenerator(seed: Long) {

  def this() = this(System.currentTimeMillis())

  case class LevelInfo(level: Level, probability: Double, prefix: String)

  def levels = levelInfos.map(_.level)
  
  val levelInfos = Array(
    LevelInfo(SEVERE,  0.05, "An error occurred"),
    LevelInfo(WARNING, 0.10, "There was a problem"),
    LevelInfo(INFO,    0.40, "Now"),
    LevelInfo(CONFIG,  0.02, "Preparing for"),
    LevelInfo(FINE,    0.15, "Prepare"),
    LevelInfo(FINER,   0.13, "Examine"),
    LevelInfo(FINEST,  0.15, "Debug for")
  )

  val messages = Array(
    "loading of resource /var/log/t/application.log",
    "printing of example log message",
    "cleaning the large brown fox that jumped over the lazy white dog",
    "looking up the path to the application file /tmp/viper/all_your_base",
    "calculating the number of atoms in the universe",
    "calculating the n^100 digit of Pi",
    "opening the viper database that doesn't exist yet"
  )

  val random = new Random(seed)

  def next(): (Level, String, Option[Exception]) = {
    val level = nextLevel()
    (level, nextMessage(level), nextException(level))
  }

  def nextLevel(): Level = {
    val randomProbability = random.nextDouble()
    var cumulativeProbability = 0.0
    for (info <- levelInfos) {
      cumulativeProbability += info.probability
      if (randomProbability < cumulativeProbability) {
        return info.level
      }
    }
    return INFO
  }

  def nextMessage(level: Level): String = {
    val prefix = levelInfos.find(_.level == level).get.prefix
    val main = messages(random.nextInt(messages.length))
    prefix + " " + main
  }

  def nextException(level: Level) = level match {
    case SEVERE => if (random.nextDouble() <= 0.6) Some(exception) else None
    case WARNING => if (random.nextDouble() <= 0.4) Some(exception) else None
    case _ => if (random.nextDouble() <= 0.05) Some(exception) else None
  }

  lazy val exception = new Exception("An unknown error occurred")

}
