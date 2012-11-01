package viper.ui

import fonts.Fonts
import javax.swing._
import java.awt.event.{WindowEvent, WindowAdapter}
import viper.util.{Prefs, EQ}
import java.awt.{Font, Dimension}

trait UI extends Prefs {

  // Self-type: Must be mixed into class that is a JFrame
  this: JFrame =>

  /** Title of the frame, also used for pulling window preferences */
  val name: String
  /** Application-specific cleanup. */
  def close(): Unit

  initLookAndFeel()
  initDefaultFont()
  initFrame()


  def setVisible() {
    restorePrefs()
    EQ.later { setVisible(true) }
  }

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

  def initDefaultFont() {
    val stream = classOf[Fonts].getResourceAsStream("Inconsolata.otf")
    if (stream == null) {
      return
    }
    val cfont = Font.createFont(Font.TRUETYPE_FONT, stream)
    val key = "defaultFont"
    val defaults = UIManager.getLookAndFeelDefaults
    val defaultSize = 14
    val defaultStyle = defaults.getFont(key).getStyle
    val font = cfont.deriveFont(defaultStyle, defaultSize)
    defaults.put(key, font)
  }

  private def initFrame() {
    setTitle(name)
    registerPrefs(name, this, new Dimension(1000, 600))
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        closing()
      }
    })
  }

  private def closing() {
    storePrefs()
    close()
  }

}
