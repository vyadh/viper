import sbt._

object Build extends Build {

  println("Using Java: " + sys.props("java.version") + " (" + sys.props("java.vendor") + ")")
  println("Using OS: " + sys.props("os.name"))

}
