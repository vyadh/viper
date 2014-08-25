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
