package viper

import domain.Subscriber
import java.net.ServerSocket
import java.io.{File, BufferedReader, InputStreamReader}

object ViperServer {
  val port = 58356
}

class ViperServer {

  def start(addSub: Subscriber => Unit) {
    val socket = new ServerSocket(ViperServer.port)
    val listener = new ClientListener(socket, addSub)
    val thread = new Thread(listener, "server")
    thread.setDaemon(true)
    thread.start()
  }

  private class ClientListener(socket: ServerSocket, addSub: Subscriber => Unit) extends Runnable {
    var running = true

    def run() {
      while (running) {
        val connection = socket.accept()
        val in = new BufferedReader(new InputStreamReader(connection.getInputStream()))
        val file = in.readLine();
        if (file == null) {
          running = false
        } else {
          addSub(subscriber(file))
        }
      }
    }
  }

  def subscriber(path: String): Subscriber = {
    val file = new File(path)
    new Subscriber("auto-file", file.getName, path)
  }

}
