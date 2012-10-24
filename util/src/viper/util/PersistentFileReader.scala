package viper.util

import java.io._

/**
 * Provides tail-like functionality reading from a file.
 * Detects if the file has been overwritten, and if so, indicates the end of the file has been
 * reached.
 */
class PersistentFileReader(path: String) extends Reader {

  val waitTime = 1000L
  val file = new File(path)
  var size = 0L
  var reader: Reader = null
  var running = true

  def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    while (running) {
      if (newFile) {
        reader = newReader()
      }

      // Keep size up-to-date to detect renames/overwrites
      size = file.length()

      val read = reader.read(cbuf, off, len)
      if (read != -1) {
        return read
      }

      Thread.sleep(waitTime)
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

    while (true) {
      try {
        return new FileReader(file)
      } catch {
        case _: FileNotFoundException => {
          // Wait a bit for file to be created again
          Thread.sleep(waitTime)
        }
      }
    }
    throw new IllegalStateException
  }

}
