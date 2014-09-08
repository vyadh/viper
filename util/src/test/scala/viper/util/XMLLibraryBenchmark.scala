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

import java.io.{File, Reader}
import java.nio.charset.Charset
import java.nio.file.Files
import javax.xml.stream.{XMLResolver, XMLInputFactory, XMLStreamReader}

import com.ctc.wstx.stax.WstxInputFactory

/**
 * Created by kieron on 08/09/2014.
 */
class XMLLibraryBenchmark(file: File) {

  // Standard
  //  3400
  //  4045
  //  2883
  //  3176
  //  3194

  // Woodstox ValidatingStreamReader
  //  2925
  //  2222
  //  2328
  //  2417
  //  2262

  // Woodstox .configureForSpeed
  //  2450
  //  1612
  //  1603
  //  1598
  //  1563

  val woodstoxLibrary = new WstxInputFactory()
  woodstoxLibrary.configureForSpeed()
  woodstoxLibrary.getConfig.doSupportDTDs(false)
  woodstoxLibrary.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  woodstoxLibrary.setProperty(XMLInputFactory.IS_VALIDATING, false)
  woodstoxLibrary.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES , false)
  woodstoxLibrary.setXMLResolver(new NoXMLResolver)

  val standardLibrary = XMLInputFactory.newInstance()
  standardLibrary.setXMLResolver(new NoXMLResolver)

  def benchLibrary(createReader: () => XMLStreamReader): Unit = {
    println("Benchmarking: " + createReader().getClass.getName)

    val times = new Benchmark(() => {
      val reader = createReader()
      while (reader.hasNext) {
        reader.next()
      }
    }).run(5)

    times.foreach(println)
  }

  def createStandardReader(): XMLStreamReader = {
    standardLibrary.createXMLStreamReader(reader)
  }

  def createWoodstoxReader(): XMLStreamReader = {
    woodstoxLibrary.createXMLStreamReader(reader)
  }

  def reader(): Reader = {
    Files.newBufferedReader(file.toPath, Charset.defaultCharset)
  }

  class NoXMLResolver extends XMLResolver {
    override def resolveEntity(p1: String, p2: String, p3: String, p4: String) = null
  }

}

object XMLLibraryBenchmark {

  def main(args: Array[String]): Unit = {
    val file = new File(args(0))
    println(file)
    val bench = new XMLLibraryBenchmark(file)
    bench.benchLibrary(bench.createStandardReader)
    bench.benchLibrary(bench.createWoodstoxReader)
  }

}

