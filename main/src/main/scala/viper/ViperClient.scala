package viper

import java.net.{ServerSocket, InetSocketAddress, Socket}
import java.io.{Closeable, IOException, DataOutputStream}

object ViperClient {

  val port = ViperServer.port

  def send(files: Array[String]): Boolean = {
    if (portOpen()) {
      try {
        writeToSocket(files)
        true
      } catch {
        case e: Exception => false
      }
    } else {
      false
    }
  }

  /** Slightly faster way to detect open port (quicker when port is not open). */
  def portOpen(): Boolean = {
    var socket: Option[ServerSocket] = None
    try {
      socket = Some(new ServerSocket(port))
      false
    } catch {
      case e: IOException => true
    } finally {
      socket.foreach(close(_))
    }
  }

  def writeToSocket(files: Array[String]) {
    val socket = new Socket
    socket.connect(new InetSocketAddress(port), 500)
    val out = new DataOutputStream(socket.getOutputStream)
    out.writeBytes(files.mkString("\n"));
    close(out)
    close(socket)
  }

  def close(closable: Closeable) {
    try {
      closable.close()
    } catch {
      case e: Exception =>
    }
  }

}
