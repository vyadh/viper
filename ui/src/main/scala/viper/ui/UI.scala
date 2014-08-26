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

import fonts.Fonts
import javax.swing._
import java.awt.event.{KeyEvent, WindowEvent, WindowAdapter}
import text.JTextComponent
import viper.util.{Prefs, EQ}
import java.awt.{KeyboardFocusManager, KeyEventPostProcessor, Font, Dimension}

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

  def initFrame() {
    setTitle(name)
    registerPrefs(name, this, new Dimension(1000, 600))
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        closing()
      }
    })
  }

  def installKeyAction(keyStroke: KeyStroke, action: => Unit) {
    // Other components consume our InputMap keys, so use post processor instead
    val manager = KeyboardFocusManager.getCurrentKeyboardFocusManager
    manager.addKeyEventPostProcessor(new KeyEventPostProcessor {
      def postProcessKeyEvent(e: KeyEvent): Boolean = {
        // Only do action on key release
        if (e.getID != KeyEvent.KEY_RELEASED) {
          return false
        }
        // Some actions conflict with currently focused components
        if (e.getSource.isInstanceOf[JTextComponent]) {
          return false
        }
        val eventKey = KeyStroke.getKeyStroke(e.getKeyCode, e.getModifiersEx)
        if (eventKey == keyStroke) {
          action
          true
        } else {
          false
        }
      }
    })

  }

  private def closing() {
    storePrefs()
    close()
  }

}
