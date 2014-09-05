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

import org.scalatest._

class StripXMLFilterReaderTwoDocsTest extends FunSuite {

  val before = """
      |<?xml version="1.0" encoding="UTF-8" standalone="no"?>
      |<!DOCTYPE
      |  log SYSTEM "logger.dtd">
      |<log>
      |<record>
      |  <date>2014-09-05T21:34:58</date>
      |  <level>INFO</level>
      |  <message>Now calculating the n^100 digit of Pi</message>
      |</record>
      |</log>
      |
      |\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008
      |
      |<?xml version="1.0" encoding="UTF-8" standalone="no"?>
      |<!DOCTYPE log SYSTEM "logger.dtd">
      |<log>
      |<record>
      |  <date>2014-09-05T21:34:58</date>
      |  <level>INFO</level>
      |  <message>Now calculating the n^100 digit of Pi</message>
      |</record>
      |</log>
      |""".stripMargin

  val after = """
      |<log>
      |<record>
      |  <date>2014-09-05T21:34:58</date>
      |  <level>INFO</level>
      |  <message>Now calculating the n^100 digit of Pi</message>
      |</record>
      |</log>
      |
      |<log>
      |<record>
      |  <date>2014-09-05T21:34:58</date>
      |  <level>INFO</level>
      |  <message>Now calculating the n^100 digit of Pi</message>
      |</record>
      |</log>
      |""".stripMargin

  test("two XML documents have PI, doctype and invalid characters stripped") {
    assert(trim(process(before)) === trim(after))
  }


  def process(text: String) = consume(reader(text))

  def consume(in: Reader): String = {
    val buf = new Array[Char](1000)
    val read = in.read(buf, 0, buf.size)
    if (read == -1) "" else new String(buf, 0, read)
  }

  def reader(text: String): StripXMLFilterReader = {
    new StripXMLFilterReader(new StringReader(text))
  }

  def trim(text: String) = text.replaceAll("[\r\n]+", "\n").trim

}
