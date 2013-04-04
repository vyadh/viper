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
