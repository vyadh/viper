package viper.util

import java.io.{Reader, StringReader}

object StripPIFilterReaderTest {

  def main(args: Array[String]) {
    assert(process("not modified") == "not modified")
    assert(process("<? removed ?>") == "")
    assert(process("<? removed ?>:end") == ":end")
    assert(process("start:<? removed ?>") == "start:")
    assert(process("foo <? removed ?> bar") == "foo  bar")
  }


  def process(text: String) = consume(reader(text))

  def consume(in: Reader): String = {
    val buf = new Array[Char](100)
    val read = in.read(buf, 0, buf.size)
    if (read == -1) "" else new String(buf, 0, read)
  }

  def reader(text: String): StripPIFilterReader = {
    new StripPIFilterReader(new StringReader(text))
  }

}
