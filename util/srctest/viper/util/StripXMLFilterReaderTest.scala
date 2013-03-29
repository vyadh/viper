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
