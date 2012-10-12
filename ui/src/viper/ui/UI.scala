package viper.ui

import javax.swing._
import java.awt.event.{WindowEvent, WindowAdapter}

trait UI extends UIComponents {

  // Self-type: Must be mixed into class that is a JFrame
  this: JFrame =>

  /** Title of the frame, also used for pulling window preferences */
  val name: String
  /** Application-specific cleanup. */
  def close(): Unit

  initFrame()


  private def initFrame() {
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        close()
      }
    })

    setTitle(name)

    initSize()
    initPosition()
  }

  def initSize() {
    // todo set from prefs
    setSize(1000, 600)
  }

  def initPosition() {
    // todo set from prefs
    setLocationRelativeTo(null)
  }

}
