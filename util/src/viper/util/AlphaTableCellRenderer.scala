package viper.util

import javax.swing.{JTable, UIManager, JLabel}
import javax.swing.table.TableCellRenderer
import java.awt._
import java.awt.image.BufferedImage

class AlphaTableCellRenderer extends JLabel with TableCellRenderer {

  val selectionBackground = UIManager.getColor("Table[Enabled+Selected].textBackground")
  val alternateRowColour = UIManager.getColor("Table.alternateRowColor")
  val border = UIManager.getBorder("Table.cellNoFocusBorder")

  val composite = AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f)
  var selected: Boolean = false

  setOpaque(true)
  setBorder(border)

  def getTableCellRendererComponent(
    table: JTable,
    value: Any,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int): Component = {

    setText(value.toString)
    setBackground(if (row % 2 == 0) Color.white else alternateRowColour)

    // Use paintComponent to paint the selection
    selected = isSelected

    this
  }

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
