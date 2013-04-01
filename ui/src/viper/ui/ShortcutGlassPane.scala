package viper.ui

import javax.swing.{KeyStroke, JFrame, JComponent}
import java.awt._
import geom.Rectangle2D
import scala.List
import event.{MouseEvent, MouseAdapter, InputEvent, KeyEvent}

class ShortcutGlassPane extends JComponent {

  val helpWidth = 400
  val helpHeight = 320

  val shortcuts = {
    import KeyStroke.getKeyStroke
    import KeyEvent._
    import InputEvent._
    val noModifiers = 0

    List(
      Shortcut(getKeyStroke(VK_R,      noModifiers),    "mark read"),
      Shortcut(getKeyStroke(VK_U,      noModifiers),    "mark unread"),
      Shortcut(getKeyStroke(VK_DELETE, noModifiers),    "delete selected"),
      Shortcut(getKeyStroke(VK_ENTER,  noModifiers),    "collapse/expand"),
      Shortcut(getKeyStroke(VK_C,      CTRL_DOWN_MASK), "copy item details"),
      Shortcut(getKeyStroke(VK_F,      CTRL_DOWN_MASK), "filter items"),
      Shortcut(getKeyStroke(VK_UP,     noModifiers),    "previous item"),
      Shortcut(getKeyStroke(VK_DOWN,   noModifiers),    "next item"),
      Shortcut(getKeyStroke(VK_LEFT,   noModifiers),    "focus subscriptions"),
      Shortcut(getKeyStroke(VK_RIGHT,  noModifiers),    "focus items"),
      Shortcut(getKeyStroke(VK_TAB,    noModifiers),    "toggle views")
    )
  }

  def install(frame: JFrame) {
    frame.setGlassPane(this)
    addActivationListeners()
  }

  def addActivationListeners() {
    val manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    manager.addKeyEventPostProcessor(new KeyEventPostProcessor {
      def postProcessKeyEvent(e: KeyEvent): Boolean = {
        if (e.getID == KeyEvent.KEY_PRESSED) {
          if (isVisible) {
            setVisible(false)
            return true
          } else if (e.getKeyCode == KeyEvent.VK_SLASH && e.isShiftDown) { // Question Mark key
            setVisible(true)
            return true
          }
        }
        false
      }
    });

    addMouseListener(new MouseAdapter {
      override def mouseReleased(e: MouseEvent) {
        if (isVisible) {
          setVisible(false)
        }
      }
    })
  }

  override def paintComponent(graphics: Graphics) {
    val g = graphics.create.asInstanceOf[Graphics2D]

    g.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    val startX = getWidth/2 - helpWidth/2
    val startY = getHeight/2 - helpHeight/2
    g.translate(startX, startY)
    g.setClip(0, 0, helpWidth, helpHeight)

    paintBackgroud(g)

    val titleY = paintTitle(g)
    g.translate(0, titleY)

    val underlineY = paintUnderline(g)
    g.translate(0, underlineY)

    paintShortcuts(g)

    g.dispose()
  }

  def paintBackgroud(g: Graphics2D) {
    g.setColor(new Color(0, 0, 0, 200))

    g.fillRoundRect(0, 0, helpWidth, helpHeight, 20, 20)
  }

  def paintTitle(g: Graphics2D): Int = {
    g.setColor(Color.white)
    g.setFont(g.getFont.deriveFont(20f))

    val title = "Keyboard Shortcuts"
    val titleBounds = g.getFontMetrics.getStringBounds(title, g)
    val titleX = (helpWidth/2 - titleBounds.getWidth/2).toInt
    val titleY = (titleBounds.getHeight + 10).toInt

    g.drawString(title, titleX, titleY)

    titleY + 10
  }

  def paintUnderline(g: Graphics2D): Int = {
    val gap = 15
    val lineStart = gap
    val lineEnd = helpWidth - gap

    g.setColor(Color.gray)
    g.drawLine(lineStart, 0, lineEnd, 0)

    10
  }

  def paintShortcuts(g: Graphics2D) {
    val bounds = shortcuts.foldLeft(new Rectangle(0, 0, 0, 0))((acc, s) => acc.createUnion(s.bounds(g)).getBounds)
    val maxKeyWidth = shortcuts.map(_.keyBounds(g).getWidth.toInt).max
    val startX = helpWidth/2 - bounds.getWidth/2
    val startY = 30

    g.translate(startX, startY)
    g.setFont(g.getFont.deriveFont(16f))

    for (si <- shortcuts.zipWithIndex) {
      val (shortcut, index) = si

      val keyBounds = shortcut.keyBounds(g).getBounds
      val keyX = maxKeyWidth - keyBounds.width
      val keyY = keyBounds.height * index // Assumes key bounds are all the same height

      g.setColor(Color.yellow)
      g.drawString(shortcut.keyText, keyX, keyY)

      g.setColor(Color.white)
      g.drawString(" : " + shortcut.description, keyX + keyBounds.width, keyY)
    }
  }

  case class Shortcut(keyStroke: KeyStroke, description: String) {

    def bounds(g: Graphics2D) = calcBounds(g, keyText + " : " + description)
    def keyBounds(g: Graphics2D) = calcBounds(g, keyText)

    def keyText: String = {
      import keyStroke.{getModifiers, getKeyCode}
      import KeyEvent._

      val modifiers =
        if (getModifiers == 0) ""
        else '<' + getKeyModifiersText(getModifiers).toLowerCase + "> + "
      val key = getKeyText(getKeyCode).toLowerCase

      modifiers + key
    }

    def calcBounds(g: Graphics2D, text: String): Rectangle2D = {
      val metrics = g.getFontMetrics(g.getFont)
      metrics.getStringBounds(text, g)
    }

  }

}
