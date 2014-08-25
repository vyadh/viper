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
