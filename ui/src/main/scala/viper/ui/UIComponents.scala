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

import images.Images
import javax.swing._
import javax.swing.event._
import java.awt._
import ca.odell.glazedlists.{BasicEventList, SortedList, FilterList, EventList}
import ca.odell.glazedlists.swing.{EventSelectionModel, TableComparatorChooser, EventTableModel, EventListModel}
import collection.mutable
import ca.odell.glazedlists.gui.{AbstractTableComparatorChooser, TableFormat}
import java.awt.event.ActionEvent
import text.DefaultCaret
import scala.Some
import viper.util.IconCache
import scala.List

trait UIComponents {

  /** Table with columns that can be sized to the data within them. */
  class Table extends JTable {
    setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN)

    def refitColumns() {
      val columnModel = getColumnModel()
      for (column <- 0 until columnModel.getColumnCount) {
        refitColumn(column);
      }
    }

    private def refitColumn(column: Int) {
      val width = math.max(headerWidth(column), dataWidth(column))
      refitColumn(column, width)
    }

    private def headerWidth(column: Int): Int = {
      val tableColumn = getColumnModel.getColumn(column)
      val renderer = tableColumn.getHeaderRenderer
      val rendererOrDefault = if (renderer == null) getTableHeader.getDefaultRenderer else renderer
      val value = tableColumn.getHeaderValue
      val component = rendererOrDefault.getTableCellRendererComponent(this, value, false, false, -1, column)
      component.getPreferredSize.width
    }

    private def dataWidth(column: Int): Int = {
      var result = 0
      val maxWidth = getColumnModel.getColumn(column).getMaxWidth
      val rows = math.min(getRowCount, 100)
      for (row <- 0 until rows) {
        result = math.max(result, dataWidth(column, 0))
        if (result > maxWidth) {
          return result // Optimisation
        }
      }
      result
    }

    private def dataWidth(column: Int, row: Int): Int = {
      val renderer = getCellRenderer(row, column)
      val component = prepareRenderer(renderer, row, column)
      component.getPreferredSize.width + getIntercellSpacing.width + 12
    }

    private def refitColumn(column: Int, width: Int) {
      val header = getTableHeader
      val tableColumn = header.getColumnModel.getColumn(column)
      header.setResizingColumn(tableColumn)

      tableColumn.setWidth(width)
      tableColumn.setPreferredWidth(width) // Stop columns resizing when re-sorted
    }
  }

  class FilterableSortableTable[T] extends Table {
    private var installed: Option[TableComparatorChooser[T]] = None
    private var selectionListeners = Seq[ListSelectionListener]()

    def selected: EventList[T] = {
      val selectionModel = getSelectionModel
      if (selectionModel.isInstanceOf[EventSelectionModel[T]]) {
        selectionModel.asInstanceOf[EventSelectionModel[T]].getSelected
      } else {
        new BasicEventList[T]
      }
    }

    def addSelectionListener(listener: => Unit) {
      if (installed.isDefined) throw new IllegalStateException("Add listeners before install")

      selectionListeners = selectionListeners ++ Seq(new ListSelectionListener {
        def valueChanged(e: ListSelectionEvent) {
          if (!e.getValueIsAdjusting) listener
        }
      })
    }

    def sort(defaultSort: List[(String, Boolean)]) {
      for {
        comparatorChooser <- installed
        (name, reverse) <- defaultSort
        index <- getColumnIndex(name)
      } {
        comparatorChooser.appendComparator(index, 0, reverse)
      }
    }

    def install(filtered: FilterList[T], sorted: SortedList[T], format: TableFormat[T]) {
      uninstall()

      setModel(new EventTableModel[T](filtered, format))
      installSelectionListeners(filtered)
      installSortedTable(sorted)
    }

    private def installSelectionListeners(eventList: EventList[T]) {
      setSelectionModel(new EventSelectionModel[T](eventList))
      selectionListeners.foreach(getSelectionModel.addListSelectionListener(_))
    }

    private def installSortedTable(eventList: SortedList[T]) {
      installed = Some(
        TableComparatorChooser.install(this, eventList, AbstractTableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD)
      )
    }

    def uninstall() {
      uninstallSelectionListeners()
      uninstallSortedTable()
    }

    private def uninstallSelectionListeners() {
      selectionListeners.foreach(getSelectionModel.removeListSelectionListener(_))
    }

    private def uninstallSortedTable() {
      installed.foreach(_.dispose())
      installed = None
    }

    def hideColumn(index: Int) {
      val cm = getColumnModel
      cm.removeColumn(cm.getColumn(index))
    }

    def deleteSelected(view: EventList[T], underlying: EventList[T]) {
      val start = System.currentTimeMillis()
      val selected = this.selected

      // If we are clearing the whole list
      if (selected.size() == view.size()) {
        view.clear()
      }
      // 2x faster way of updating when the selection is contiguous
      else if (isSelectionContiguous()) {
        val from = view.indexOf(selected.get(0))
        val to = view.indexOf(selected.get(selected.size()-1))
        view.subList(from, to).clear()
      }
      // Last resort (slow!)
      else {
        underlying.removeAll(selected)
      }

      // todo remove timings
      val end = System.currentTimeMillis()
      println(end-start)
    }

    private def isSelectionContiguous(): Boolean = {
      //todo
      false
    }

    private def getColumnIndex(name: String): Option[Int] = {
      for (i <- 0 until getColumnCount) {
        if (getColumnName(i) == name) {
          return Some(i)
        }
      }
      None
    }
  }

  class ScrollPane(c: JComponent) extends JScrollPane(c) {
    // Ensures background is white when inner table does not fill the scroll pane
    getViewport().setBackground(Color.WHITE);
  }

  class ToolBar extends JToolBar {
    setFloatable(false)
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) // Fix for Nimbus bug 7171632

    override def add(c: Component): Component = {
      super.add(Box.createHorizontalStrut(5))
      super.add(c)
    }

    def addFiller() {
      add(Box.createHorizontalGlue())
    }
  }

  class ListPanel[T](eventList: EventList[T], onSelection: T => Unit)
        extends JList[T](new EventListModel[T](eventList).asInstanceOf[ListModel[T]]) {

    private val selectionModel = new EventSelectionModel(eventList)

    def selected = selectionModel.getSelected

    def selected_=(t: T) {
      val index = eventList.indexOf(t)
      selectionModel.setSelectionInterval(index, index)
    }

    setSelectionModel(selectionModel)
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent) {
        if (e.getValueIsAdjusting) return
        Option(getSelectedValue).foreach(onSelection(_))
      }
    })
  }

  class CardPanel extends JPanel(new CardLayout) { //todo no longer needed
    val cards = getLayout.asInstanceOf[CardLayout]
    /** Cards in layout not accessible, so keep mapping */
    val mapping = new mutable.HashMap[String, java.awt.Component]

    override def add(name: String, component: java.awt.Component): Component = {
      mapping.put(name, component)
      super.add(name, component)
    }

    def remove(name: String) {
      for (component <- mapping.remove(name)) {
        remove(component)
      }
    }

    def show(name: String) {
      cards.show(this, name)
    }
  }

  case class VerticalSplitPane[T <: JComponent, B <: JComponent](top: T, bottom: B)
        extends JSplitPane(JSplitPane.VERTICAL_SPLIT) with SplitPane {

    setTopComponent(top)
    setBottomComponent(bottom)
    setResizeWeight(1.0)
  }

  case class HorizontalSplitPane[L <: JComponent, R <: JComponent](left: L, right: R)
        extends JSplitPane(JSplitPane.HORIZONTAL_SPLIT) with SplitPane {

    setLeftComponent(left)
    setRightComponent(right)
    setResizeWeight(0.0)
  }

  trait SplitPane {
    this: JSplitPane =>

    setOneTouchExpandable(true)
    setContinuousLayout(true)
  }

  class SearchBox(onChange: String => Unit) extends JTextField {

    /** Record the last text so that we only trigger updates when it has changed. */
    var last = getText
    /** When to do on changes to text. */
    val listener = new DocumentListener {
      def insertUpdate(e: DocumentEvent) { update() }
      def removeUpdate(e: DocumentEvent) { update() }
      def changedUpdate(e: DocumentEvent) { }
    }

    val dimension = new Dimension(200, 25)
    setPreferredSize(dimension)
    setMaximumSize(dimension)
    getDocument.addDocumentListener(listener)

    /** Trigger updates, but only when needed. */
    private def update() {
      val text = getText
      if (text != last) {
        last = text
        onChange(text)
      }
    }

    /** Override so updates are not triggered when the search box is updated programmatically.
      * Any filtering is only kicked off when user changes the text, otherwise we assume it is already filtered. */
    override def setText(t: String) {
      getDocument.removeDocumentListener(listener)
      super.setText(t)
      getDocument.addDocumentListener(listener)
    }
  }

  class Slider extends JSlider(0, 1) {
    setMajorTickSpacing(1)
    setPaintTicks(true)
    setPaintTrack(true)
    setSnapToTicks(true)

    addChangeListener(new ChangeListener {
      def stateChanged(e: ChangeEvent) {
        if (!getValueIsAdjusting) {
          onChange(getValue)
        }
      }
    })

    def onChange(value: Int) { }

    def restore(value: Int) {
      setValueIsAdjusting(false)
      setValue(value)
      setValueIsAdjusting(true)
    }
  }

  class TextArea extends JTextArea {
    // Don't scroll to bottom/right of area on large amounts of text
    getCaret.asInstanceOf[DefaultCaret].setUpdatePolicy(DefaultCaret.NEVER_UPDATE)

    setLineWrap(true)
  }

  class EmptyBorder(size: Int) extends javax.swing.border.EmptyBorder(size, size, size, size)

  class BasicAction(name: String, action: => Unit) extends AbstractAction(name) {
    def actionPerformed(e: ActionEvent) {
      action
    }
  }

  class MenuAction(name: String, action: => Unit) extends BasicAction(name, action) {
    putValue(Action.SHORT_DESCRIPTION, name)
    putValue(Action.SMALL_ICON, iconSVG(name, 18))
    putValue(Action.LARGE_ICON_KEY, iconSVG(name, 28))
  }

  def iconSVG(name: String, size: Int): Icon = {
    val key = name.replace(" ", "").toLowerCase
    val file = key + ".svg"
    val stream = classOf[Images].getResourceAsStream(file)
    IconCache.load(key, size, stream)
  }

  def iconToImage(icon: Icon): Image = icon match {
    case icon: ImageIcon => icon.getImage
    case icon => {
      val w = icon.getIconWidth
      val h = icon.getIconHeight
      val ge = GraphicsEnvironment.getLocalGraphicsEnvironment
      val gc = ge.getDefaultScreenDevice.getDefaultConfiguration
      val image = gc.createCompatibleImage(w, h)
      val g = image.createGraphics
      icon.paintIcon(null, g, 0, 0)
      g.dispose()
      image
    }
  }

  /** Tell GL to repaint. */
  def fireUpdate[T](list: EventList[T], item: T) {
    list.set(list.indexOf(item), item)
  }

}
