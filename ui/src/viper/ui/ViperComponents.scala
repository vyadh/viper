package viper.ui

import ca.odell.glazedlists._
import calculation.{Calculations, Calculation}
import viper.domain._
import javax.swing.{JTable, BoundedRangeModel, JLabel, JTextArea}
import java.awt.Dimension
import matchers.{Matcher, TextMatcherEditor}
import ca.odell.glazedlists.swing.GlazedListsSwing
import viper.domain.Subscription
import java.util

trait ViperComponents extends UIComponents {

  case class MainComponents(
    subscriptionList: ListPanel[Subscribed],
    severitySlider: SeveritySlider,
    searchBox: SearchBox,
    table: RecordTable,
    preview: JTextArea
  )

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

  class RecordTable(onSelection: EventList[Record] => Unit, onColumnWidthChange: JTable => Unit)
        extends FilterableSortableTable[Record] {

    setDefaultRenderer(classOf[Object], new RecordTableCellRender)
    addSelectionListener { onSelection(selected) }
    getColumnModel.addColumnModelListener(new TableColumnWidthListener(this, onColumnWidthChange(RecordTable.this)))

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

}
