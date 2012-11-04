package viper.ui

import images.Images
import javax.swing._
import javax.swing.event._
import java.awt._
import ca.odell.glazedlists.{SortedList, FilterList, EventList}
import ca.odell.glazedlists.swing.{EventSelectionModel, TableComparatorChooser, EventTableModel, EventListModel}
import collection.mutable
import ca.odell.glazedlists.gui.{AbstractTableComparatorChooser, TableFormat}
import java.awt.event.ActionEvent
import javax.imageio.ImageIO
import text.DefaultCaret
import scala.Some
import com.kitfox.svg.SVGCache
import com.kitfox.svg.app.beans.SVGIcon

trait UIComponents {

  class FilterableSortableTable[T] extends JTable {
    private var installed: Option[TableComparatorChooser[T]] = None
    private var selectionListeners = Seq[ListSelectionListener]()

    def selected = getSelectionModel.asInstanceOf[EventSelectionModel[T]].getSelected

    def addSelectionListener(listener: => Unit) {
      if (installed.isDefined) throw new IllegalStateException("Add listeners before install")

      selectionListeners = selectionListeners ++ Seq(new ListSelectionListener {
        def valueChanged(e: ListSelectionEvent) {
          if (!e.getValueIsAdjusting) listener
        }
      })
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
        TableComparatorChooser.install(this, eventList, AbstractTableComparatorChooser.SINGLE_COLUMN)
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
      if (selected.isEmpty || selected.size() == view.size()) {
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
  }

  class ScrollPane(c: JComponent) extends JScrollPane(c) {
    // Ensures background is white when inner table does not fill the scroll pane
    getViewport().setBackground(Color.WHITE);
  }

  class ToolBar extends JToolBar {
    setFloatable(false)
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS)) // Fix for Nimbus bug 7085425

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

    val dimension = new Dimension(200, 20)
    //    setSize(dimension)
    setPreferredSize(dimension)
    setMaximumSize(dimension)
    //    setMinimumSize(dimension)
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

  class SimpleAction(name: String, action: => Unit) extends AbstractAction(name) {
    putValue(Action.SHORT_DESCRIPTION, name)
    putValue(Action.SMALL_ICON, iconSVG(name, 18))
    putValue(Action.LARGE_ICON_KEY, iconSVG(name, 28))

    def actionPerformed(e: ActionEvent) {
      action
    }
  }

  def icon(name: String, size: Int): Icon = {
    val file = name.replace(" ", "").toLowerCase + '_' + size + ".png"
    val stream = classOf[Images].getResourceAsStream(file)
    if (stream == null) new ImageIcon else new ImageIcon(ImageIO.read(stream))
  }

  def iconSVG(name: String, size: Int): Icon = {
    val file = name.replace(" ", "").toLowerCase + ".svg"
    val stream = classOf[Images].getResourceAsStream(file)
    val svgURI = SVGCache.getSVGUniverse.loadSVG(stream, name)
    val icon = new SVGIcon
    icon.setSvgURI(svgURI)
    icon.setScaleToFit(true)
    icon.setAntiAlias(true)
    val height = size
    val width = ((size.toDouble / icon.getIconHeight) * icon.getIconWidth).toInt
    icon.setPreferredSize(new Dimension(width, height))
    icon
  }

  /** Tell GL to repaint. */
  def fireUpdate[T](list: EventList[T], item: T) {
    list.set(list.indexOf(item), item)
  }

  class TableColumnWidthListener(table: JTable, onChange: => Unit) extends TableColumnModelListener {
    def columnMarginChanged(e: ChangeEvent) {
      // Only react to user-driven column changes
      if (table.getTableHeader.getResizingColumn != null) {
        onChange
      }
    }

    def columnAdded(e: TableColumnModelEvent) {}
    def columnRemoved(e: TableColumnModelEvent) {}
    def columnMoved(e: TableColumnModelEvent) {}
    def columnSelectionChanged(e: ListSelectionEvent) {}
  }

}
