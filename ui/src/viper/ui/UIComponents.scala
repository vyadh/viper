package viper.ui

import javax.swing._
import event._
import java.awt.{Dimension, Component, CardLayout, Color}
import ca.odell.glazedlists.{SortedList, FilterList, EventList}
import ca.odell.glazedlists.swing.{EventSelectionModel, TableComparatorChooser, EventTableModel, EventListModel}
import collection.mutable
import ca.odell.glazedlists.gui.{AbstractTableComparatorChooser, TableFormat}
import java.awt.event.ActionEvent
import javax.imageio.ImageIO

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
  }

  case class HorizontalSplitPane[L <: JComponent, R <: JComponent](left: L, right: R)
        extends JSplitPane(JSplitPane.HORIZONTAL_SPLIT) with SplitPane {

    setLeftComponent(left)
    setRightComponent(right)
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

  class EmptyBorder(size: Int) extends javax.swing.border.EmptyBorder(size, size, size, size)

  class SimpleAction(name: String, action: => Unit) extends AbstractAction(name) {
    putValue(Action.SHORT_DESCRIPTION, name)
    putValue(Action.SMALL_ICON, icon(name, 16))
    putValue(Action.LARGE_ICON_KEY, icon(name, 28))

    def actionPerformed(e: ActionEvent) {
      action
    }
  }

  def icon(name: String, size: Int): Icon = {
    val file = name.replace(" ", "").toLowerCase + '_' + size + ".png"
    new ImageIcon(ImageIO.read(this.getClass.getResourceAsStream("images/" + file)))
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
