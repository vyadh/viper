package viper

import java.net.ServerSocket
import java.io.{BufferedReader, InputStreamReader}

object ViperServer {
  val port = 58356
}

class ViperServer {

  def start(consumer: String => Unit) {
    val socket = new ServerSocket(ViperServer.port)
    val listener = new ClientListener(socket, consumer)
    val thread = new Thread(listener, "server")
    thread.setDaemon(true)
    thread.start()
  }

  private class ClientListener(socket: ServerSocket, consumer: String => Unit) extends Runnable {
    var running = true

    def run() {
      while (running) {
        val connection = socket.accept()
        val in = new BufferedReader(new InputStreamReader(connection.getInputStream()))
        val line = in.readLine();
        if (line == null) {
          running = false
        } else {
          consumer(line)
        }
      }
    }
  }

}
