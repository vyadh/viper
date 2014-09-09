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

import org.scalatest.{Matchers, FunSuite}

/**
 * Created by kieron on 07/09/2014.
 */
class PersistentFileReaderTest extends FunSuite with Matchers {

  test("read empty file") {
    val writer = new LineWriter
    val reader = persistentReader(writer)

    reader.readLine() should equal (null)
  }

  test("read single line") {
    val writer = new LineWriter
    val reader = persistentReader(writer)

    writer.writeLine("single line")
    reader.readLine() should equal ("single line")
  }

  test("read multiple lines") {
    val writer = new LineWriter
    val reader = persistentReader(writer)

    writer.writeLine("1st line")
    writer.writeLine("2nd line")
    writer.writeLine("3rd line")

    reader.readLine() should equal ("1st line")
    reader.readLine() should equal ("2nd line")
    reader.readLine() should equal ("3rd line")
    reader.readLine() should equal (null)
  }

  test("read multiple lines around no content") {
    val writer = new LineWriter
    val reader = persistentReader(writer)

    writer.writeLine("1st line")
    reader.readLine() should equal ("1st line")

    reader.readLine() should equal (null)

    writer.writeLine("2nd line")
    reader.readLine() should equal ("2nd line")
  }


  def persistentReader(writer: LineWriter): BufferedReader = {
    val path = writer.file.getAbsolutePath
    val reader = new PersistentFileReader(path, blockOnEOF = false)
    new BufferedReader(reader)
  }

  class LineWriter {
    val file = File.createTempFile(getClass.getName, null)
    val writer = new BufferedWriter(new FileWriter(file))

    def writeLine(line: String): Unit = {
      writer.write(line)
      writer.newLine()
      writer.flush()
    }
  }
  
}
