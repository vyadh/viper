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

import java.io.{Reader, StringReader}

object StripXMLFilterReaderTest {

  def main(args: Array[String]) {
    assertStrippedPI()
    assertStrippedInvalidChars()
  }

  def assertStrippedPI() {
    assert(process("not modified") == "not modified")
    assert(process("<? removed ?>") == "")
    assert(process("<? removed ?>:end") == ":end")
    assert(process("start:<? removed ?>") == "start:")
    assert(process("foo <? removed ?> bar") == "foo  bar")
  }

  def assertStrippedInvalidChars() {
    assert(process("foo \u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008 bar") == "foo  bar")
  }


  def process(text: String) = consume(reader(text))

  def consume(in: Reader): String = {
    val buf = new Array[Char](100)
    val read = in.read(buf, 0, buf.size)
    if (read == -1) "" else new String(buf, 0, read)
  }

  def reader(text: String): StripXMLFilterReader = {
    new StripXMLFilterReader(new StringReader(text))
  }

}
