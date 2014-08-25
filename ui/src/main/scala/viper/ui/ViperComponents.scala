package viper.ui

import ca.odell.glazedlists._
import calculation.{Calculations, Calculation}
import viper.domain._
import javax.swing._
import java.awt.{Dimension}
import matchers.{Matcher, TextMatcherEditor}
import ca.odell.glazedlists.swing.GlazedListsSwing
import java.util
import viper.domain.Subscription
import java.awt.event.KeyEvent
import viper.util.EQ

trait ViperComponents extends UIComponents {

  case class MainComponents(
    subscriptionList: ListPanel[Subscribed],
    severitySlider: SeveritySlider,
    searchBox: SearchBox,
    tableWithPreview: TableWithPreview
  ) {
    def table = tableWithPreview.table
    def preview = tableWithPreview.preview
  }

  case class ViewObjects(
    subscription: Subscription,
    format: RecordTableFormat,
    data: EventList[Record],
    severitied: ThresholdList[Record],
    sorted: SortedList[Record],
    filtered: FilterList[Record],
    filterer: TextMatcherEditor[Record],
    subscribed: Subscribed,
    var currentSearchFilter: String = "",
    var currentSeverityFilter: Severity = Severities.all
  ) {
    def eventLists = Seq(filtered, sorted, severitied, data)
  }

  class SubscribedList(subscribedEventList: EventList[Subscribed], onSelection: Subscribed => Unit)
    extends ListPanel[Subscribed](subscribedEventList, onSelection) {

    setCellRenderer(new SubscribedCellRenderer)
  }

  class RecordTable(onSelection: EventList[Record] => Unit) extends FilterableSortableTable[Record] {
    setDefaultRenderer(classOf[Object], new RecordTableCellRender)
    addSelectionListener { onSelection(selected) }

    // Line border between rows
    setIntercellSpacing(new Dimension(0, 1))
    setBackground(ColorScheme.tableSeparator)
  }

  class SeveritySlider(onSeverityChange: Severity => Unit) extends Slider {
    setMaximumSize(new Dimension(80, 20))
    val label = new JLabel("Severity")

    override def onChange(value: Int) {
      onSeverityChange(Severities.values.find(value == _.ordinal).get)
    }

    def install(thresholdList: ThresholdList[Record], current: Severity) {
      // todo can we get filtering to happen on separate thread?
      // todo slider can be jerky with 100,000 elements ;)
      val model = GlazedListsSwing.lowerRangeModel(thresholdList)
      // Force update using adjusting
      // (possibly bug in GL, setRangeProperties looks at ThresholdList max for change, not max of super
      setRangeProperties(model, current, true)
      setRangeProperties(model, current, false)
      setModel(model)

      setValue(current.ordinal)
    }

    private def setRangeProperties(model: BoundedRangeModel, current: Severity, adjusting: Boolean) {
      model.setRangeProperties(
        current.ordinal,
        Severities.min.ordinal,
        Severities.min.ordinal,
        Severities.max.ordinal,
        adjusting)
    }
  }

  class RecordSeverityThresholdEvaluator extends ThresholdList.Evaluator[Record] {
    def evaluate(record: Record) = record.severity.ordinal
  }

  protected def thresholdList(eventList: EventList[Record]): ThresholdList[Record] =
    new ThresholdList[Record](eventList, new RecordSeverityThresholdEvaluator())

  protected def sortedList[T](eventList: EventList[T]) =
    new SortedList[T](eventList, null)

  protected def filteredList(prototype: RecordPrototype, eventList: SortedList[Record]):
        (TextMatcherEditor[Record], FilterList[Record]) = {

    val filterator = new TextFilterator[Record] {
      def getFilterStrings(baseList: util.List[String], element: Record) {
        for (field <- prototype.fields) {
          baseList.add(field.value(element).toString)
        }
      }
    }
    val textMatcherEditor = new TextMatcherEditor[Record](filterator)
    val filteredEventList = new FilterList[Record](eventList, textMatcherEditor)

    (textMatcherEditor, filteredEventList)
  }

  def calculationUnread(list: EventList[Record]): Calculation[Integer] = {
    Calculations.count(list, new Matcher[Record] {
      def matches(item: Record) = item match {
        case r: Readable => !r.read
        case _ => true
      }
    })
  }

  class TableWithPreview(val table: RecordTable, val preview: JTextArea)
        extends VerticalSplitPane(new ScrollPane(table), new ScrollPane(preview)) {

    /** Remember the divider location, and use to decide if we are expended or not. */
    var expandedLocation = -1
    /** Remember the selected row, so selection can be restored when expanded. */
    var selected = -1

    // Install Actions
    table.getInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ToggleExpansion")
    table.getActionMap.put("ToggleExpansion", new BasicAction("ToggleExpansion", toggle))

    def reset() {
      // If table model changes when we are collapsed, revert to expanded view
      if (!expanded) {
        expand()
      }

      // The model has changed, and any current preview doesn't make much sense now
      preview.setText("")
    }

    def toggle() {
      if (expanded) {
        contract()
      } else {
        expand()
      }
    }

    def expanded = expandedLocation == -1

    def contract() {
      // Avoid header jumping due to scroll bar control appearing
      if (!topScrollPane.getVerticalScrollBar.isVisible) {
        topScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
      }

      // Collapse split pane, but remember where divider was
      expandedLocation = getDividerLocation
      setDividerLocation(collapsedHeight)

      // Make sure selected row is visible
      selected = table.getSelectedRow
      scrollToRow(selected)

      // The selection makes details harder to see when collapsed
      table.getSelectionModel.clearSelection()
    }

    def expand() {
      // Restore scroll bar control setting
      topScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)

      // Restore expanded divider location
      setDividerLocation(expandedLocation)
      expandedLocation = -1

      // Restore selection
      table.getSelectionModel.setSelectionInterval(selected, selected)
    }

    // Resize split pane to show just one table row (header + one row + scroll pane insets)
    private def collapsedHeight = table.getTableHeader.getHeight + table.getRowHeight + topScrollInsets
    private def topScrollInsets = topScrollPane.getInsets.top + topScrollPane.getInsets.bottom
    private def topScrollPane = getTopComponent.asInstanceOf[ScrollPane]

    private def scrollToRow(row: Int) {
      EQ.later {
        table.scrollRectToVisible(table.getCellRect(row, 0, true));
      }
    }

  }

}
