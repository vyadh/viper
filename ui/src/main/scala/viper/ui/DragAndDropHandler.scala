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
package viper.ui

import javax.swing.{JFrame, TransferHandler}
import javax.swing.TransferHandler.TransferSupport
import java.awt.datatransfer.{UnsupportedFlavorException, DataFlavor}
import java.io.{IOException, File}
import collection.JavaConversions

object DragAndDropHandler {

  def install(frame: JFrame, consumer: File => Unit) {
    frame.setTransferHandler(new Handler(consumer))
  }

  class Handler(consumer: File => Unit) extends TransferHandler {

    override def canImport(support: TransferSupport): Boolean = {
      support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
    }

    override def importData(support: TransferSupport): Boolean = {
      if (!canImport(support)) {
        return false;
      }

      val transferable = support.getTransferable

      try {
        val data = transferable.getTransferData(DataFlavor.javaFileListFlavor)
        val files = data.asInstanceOf[java.util.List[File]]

        for (file <- JavaConversions.collectionAsScalaIterable(files)) {
          consumer(file)
        }

      } catch {
        case e: UnsupportedFlavorException => false
        case e: IOException => false
      }

      true
    }
  }

}
