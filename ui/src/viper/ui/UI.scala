package viper.ui

import javax.swing._
import java.awt.event.{WindowEvent, WindowAdapter}
import java.util.prefs.Preferences
import javax.swing.UIManager.LookAndFeelInfo

trait UI extends UIComponents {

  // Self-type: Must be mixed into class that is a JFrame
  this: JFrame =>

  /** Title of the frame, also used for pulling window preferences */
  val name: String
  /** Application-specific cleanup. */
  def close(): Unit

  private lazy val prefs = Preferences.userNodeForPackage(getClass)

  initLookAndFeel()
  initFrame()


  private def initLookAndFeel() {
    try {
      UIManager.getInstalledLookAndFeels().find(_.getName == "Nimbus").foreach { lf =>
        UIManager.setLookAndFeel(lf.getClassName)
      }
    } catch {
      // No big deal, I can't change the L&F. Maybe it's not available on this version of Java
      case e: UnsupportedLookAndFeelException =>
      case e: ClassNotFoundException =>
      case e: InstantiationException =>
      case e: IllegalAccessException =>
    }
  }

  private def initFrame() {
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        save()
        close()
      }
    })

    setTitle(name)

    initSize()
    initPosition()
  }

  def initSize() {
    val width = prefs.getInt(name + ".frame.width", 1000)
    val height = prefs.getInt(name + ".frame.height", 600)
    setSize(width, height)
  }

  def initPosition() {
    val x = prefs.getInt(name + ".frame.x", -1)
    val y = prefs.getInt(name + ".frame.y", -1)
    if (x == -1 || y == -1) {
      setLocationRelativeTo(null)
    } else {
      setLocation(x, y)
    }
  }

  def save() {
    prefs.putInt(name + ".frame.width", getSize.width)
    prefs.putInt(name + ".frame.height", getSize.height)
    prefs.putInt(name + ".frame.x", getLocation.x)
    prefs.putInt(name + ".frame.y", getLocation.y)
  }

}
