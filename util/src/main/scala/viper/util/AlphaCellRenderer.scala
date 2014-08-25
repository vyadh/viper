package viper.util

import javax.swing.JLabel
import java.awt._
import java.awt.image.BufferedImage

abstract class AlphaCellRenderer extends JLabel {

  val selectionBackground: Color
  var selected: Boolean = false
  private val composite = AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f)

  setOpaque(true)

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)

    if (selected) {
      val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
      val g2 = image.createGraphics()
      g2.setComposite(composite)
      g2.setColor(selectionBackground)
      g2.fillRect(0, 0, getWidth, getHeight)
      g2.dispose()
      g.drawImage(image, 0, 0, null)
    }
  }


  // Optimisations as in DefaultTableCellRenderer

  override def invalidate() { }
  override def validate() { }
  override def revalidate() { }
  override def repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) { }
  override def repaint(r: Rectangle) { }
  override def repaint() { }
  override def firePropertyChange(propertyName: String, oldValue: Int, newValue: Int) { }

}
