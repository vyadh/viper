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

import java.io._
import java.nio.charset.Charset
import java.nio.file.Files

/**
 * Provides tail-like functionality reading from a file.
 * Detects if the file has been overwritten, and if so, indicates the end of the
 * file has been reached.
 */
class PersistentFileReader(path: String, blockOnEOF: Boolean) extends Reader {

  val waitTime = 300L
  val file = new File(path)
  var size = 0L
  var reader: Reader = null
  var running = true

  def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    while (running) {
      if (newFile) {
        try {
          reader = newReader()
        } catch {
          case _: ReaderClosedSignal => return -1
        }
      }

      // Keep size up-to-date to detect renames/overwrites
      size = file.length()

      val read = reader.read(cbuf, off, len)
      if (read == -1 && blockOnEOF) {
        Thread.sleep(waitTime)
      } else {
        return read
      }
    }
    -1
  }

  def close() {
    running = false
    closeReader()
  }

  private def closeReader() {
    if (reader != null) {
      reader.close()
    }
  }

  private def newFile = reader == null || file.length() < size

  private def newReader(): Reader = {
    try {
      closeReader()
    } catch {
      case _: IOException =>
    }

    while (running) {
      try {
        // Use of NIO required here to avoid read locks on Windows
        // https://bugs.openjdk.java.net/browse/JDK-6357433
        return Files.newBufferedReader(file.toPath, Charset.defaultCharset)
      } catch {
        case _: FileNotFoundException => {
          // Wait a bit for file to be created again
          Thread.sleep(waitTime)
        }
      }
    }
    throw new ReaderClosedSignal
  }

  class ReaderClosedSignal extends Exception

}
