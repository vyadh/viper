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

import java.io.File
import java.util.logging._

/** Log file generation for testing. */
class LogFileGenerator(name: String, limit: Int, count: Int) {

  def tmpDir = new File(System.getProperty("java.io.tmpdir"), "viper")
  def pattern = new File(tmpDir, name + "_%g.log").getAbsolutePath
  def file0 = pattern.replace("%g", "0")

  val messages = new LogRecordGenerator

  val logger = {
    val l = Logger.getLogger("generated")
    l.addHandler(fileHandler)
    l.setLevel(Level.ALL)
    l.getHandlers.foreach(_.setLevel(Level.ALL))
    l
  }

  def fileHandler: Handler = {
    tmpDir.mkdirs()
    val h = new FileHandler(pattern, limit, count, false)
    h.setFormatter(new XMLFormatter)
    h
  }

  def logRandom(): Unit = {
    val (level, message, exception) = messages.next()
    exception match {
      case Some(e) => logger.log(level, message, e)
      case None => logger.log(level, message)
    }
  }

  def close(): Unit = {
    tmpDir.listFiles().foreach(_.delete())
    tmpDir.delete()
  }

}

object LogFileGenerator {

  def main(args: Array[String]) {
    val fileGenerator = new LogFileGenerator("file", 256 * 1024, 10)

    val logger = fileGenerator.logger
    for (_ <- 1 to 1000000) {
      fileGenerator.logRandom()
      Thread.sleep(fileGenerator.messages.random.nextInt(1500))
//      Thread.sleep(fileGenerator.messages.random.nextInt(5))
    }

//    generator.close()
  }

}
