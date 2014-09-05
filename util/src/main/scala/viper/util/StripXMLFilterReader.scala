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

import java.io.{Reader, FilterReader}

/**
 * Strips characters that would invalid XML in a streaming context.
 */
class StripXMLFilterReader(in: Reader) extends FilterReader(in) {

  /** If we are in an XML processing instruction. */
  var inPI = false
  /** If we are in an XML entity reference. */
  var inER = false
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
        if (!inPI && !inER) {

          // Check for PI
          if (containsAt(cbuf, "<?", i)) {
            inPI = true
            i += 1
          }

          // Check for ER
          else if (containsAt(cbuf, "<!", i)) {
            inER = true
            i += 1
          }

          // A normal character to include
          else if (isValidChar(cbuf(i))) {
            cbuf(last) = cbuf(i)
            last += 1
          }

          // Any other character is invalid and should be stripped
          else {
            i += 1
          }
        }

        // Currently within PI, so check for end
        else if (inPI && containsAt(cbuf, "?>", i)) {
          inPI = false
          i += 1
        }

        // Currently within ER, so check for end
        else if (inER && containsAt(cbuf, ">", i)) {
          inER = false
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

  def isValidChar(c: Char): Boolean = {
    // From: http://en.wikipedia.org/wiki/Valid_characters_in_XML
    // U+0009, U+000A, U+000D: these are the only C0 controls accepted in XML 1.0;
    // U+0020–U+D7FF, U+E000–U+FFFD: this excludes some (not all) non-characters in the BMP (all surrogates, U+FFFE and U+FFFF are forbidden);
    // U+10000–U+10FFFF: this includes all code points in supplementary planes, including non-characters.
    (c >= 0x0020 && c <= 0xD7FF) ||
    (c == 0x0009) ||
    (c == 0x000A) ||
    (c == 0x000D) ||
    (c >= 0xE000 && c <= 0xFFFD) ||
    (c >= 0x10000 && c <= 0x10FFFF)
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
