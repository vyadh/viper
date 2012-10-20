package viper.ui

import javax.swing._
import event.{CaretEvent, CaretListener, ListSelectionEvent, ListSelectionListener}
import java.awt.{Dimension, Component, CardLayout, Color}
import ca.odell.glazedlists.{SortedList, FilterList, EventList}
import ca.odell.glazedlists.swing.{EventSelectionModel, TableComparatorChooser, EventTableModel, EventListModel}
import collection.mutable
import ca.odell.glazedlists.gui.{AbstractTableComparatorChooser, TableFormat}

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

    def uninstallSortedTable() {
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
    setSelectionModel(selectionModel)
    val selected = selectionModel.getSelected

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent) {
        if (e.getValueIsAdjusting) return
        Option(getSelectedValue).foreach(onSelection(_))
      }
    })
  }

  class CardPanel extends JPanel(new CardLayout) {
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
    var restoring = false

    val dimension = new Dimension(200, 20)
    //    setSize(dimension)
    setPreferredSize(dimension)
    setMaximumSize(dimension)
    //    setMinimumSize(dimension)

    addCaretListener(new CaretListener {
      def caretUpdate(e: CaretEvent) {
        if (isEnabled && !restoring) {
          onChange(getText)
        }
      }
    })

    /** Restore search text, without triggering an update.
      * Needs to be called on event thread. */
    def restore(text: String) {
      restoring = true
      setText(text)
      restoring = false
    }
  }

  class Slider(min: Int, max: Int) extends JSlider(min, max) {
    setMajorTickSpacing(1)
    setPaintTicks(true)
    setPaintTrack(true)
    setSnapToTicks(true)
  }

  class EmptyBorder(size: Int) extends javax.swing.border.EmptyBorder(size, size, size, size)

}
