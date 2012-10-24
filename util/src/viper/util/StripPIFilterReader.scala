package viper.util

import java.io.{Reader, FilterReader}

class StripPIFilterReader(in: Reader) extends FilterReader(in) {

  /** If we are in an XML processing instruction. */
  var inPI = false
  /** PI's can legitimately be contained by CDATA. */
  var inCDATA = false //todo <![CDATA[escaped]]>

  /**
   * Reads buffers of characters, the does some filtering on that same buffer.
   */
  override def read(cbuf: Array[Char], from: Int, len: Int): Int = {
    var count = 0

    // Loop to avoid returning zero characters
    while (count == 0) {
      // Read some characters
      count = in.read(cbuf, from, len)

      // Detect and return EOF
      if (count == -1) {
        return -1
      }

      // Loop through characters
      var last = from
      var i = from
      while (i < from + count) {
        if (!inPI) {

          // Check for PI
          if (containsAt(cbuf, "<?", i)) {
            inPI = true
            i += 1
          }

          // A normal character to include
          else {
            cbuf(last) = cbuf(i)
            last += 1
          }

        }

        // Currently within PI, so check for end
        else {
          if (containsAt(cbuf, "?>", i)) {
            inPI = false
            i += 1
          }
        }

        i += 1
      }

      // Calculate how many characters we will be returning (see condition on while loop)
      count = last - from
    }

    count
  }

  def containsAt(cbuf: Array[Char], text: String, off: Int): Boolean = {
    // If the PI is on the boundary of two reads, it will not be detected
    // This probably not a problem though, as the PI is always at the start of the XML
    if (cbuf.size - off < text.length) {
      return false
    }
    for (i <- 0 until text.length) {
      if (cbuf(i+off) != text.charAt(i)) {
        return false
      }
    }
    return true
  }

  /**
   * Implemented in terms of the read method above.
   **/
  override def read(): Int = {
    val buf = new Array[Char](1)
    val result = read(buf, 0, 1)
    if (result == -1) {
      return -1
    }
    else {
      return buf(0)
    }
  }

}
