package viper.ui

import ca.odell.glazedlists.{ThresholdList, FilterList, SortedList, EventList}
import viper.domain._
import javax.swing.{JLabel, JTextArea}
import java.awt.Dimension
import ca.odell.glazedlists.matchers.TextMatcherEditor
import ca.odell.glazedlists.swing.GlazedListsSwing
import viper.domain.Subscriber
import viper.domain.Subscription

trait ViperComponents extends UIComponents {

  case class MainComponents(
    subscriptionList: ListPanel[Subscriber],
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
    var filter: String
  )

  class SubscriberList(subscriberEventList: EventList[Subscriber], onSelection: Subscriber => Unit)
    extends ListPanel[Subscriber](subscriberEventList, onSelection) {

    setCellRenderer(new SubscriberCellRenderer)
  }

  class RecordTable(preview: JTextArea) extends FilterableSortableTable[Record] {
    setDefaultRenderer(classOf[Object], new RecordTableCellRender)
    addSelectionListener { preview.setText(first.body) }

    def first = selected.get(0)
  }

  class SeveritySlider extends Slider(0, 5) {
    setMaximumSize(new Dimension(80, 20))
    val label = new JLabel("Severity")

    def install(thresholdList: ThresholdList[Record]) {
      val model = GlazedListsSwing.lowerRangeModel(thresholdList);
      model.setRangeProperties(Severities.min.ordinal, 1, Severities.min.ordinal, Severities.max.ordinal, false)
      setModel(model)
    }
  }

  class RecordSeverityThresholdEvaluator extends ThresholdList.Evaluator[Record] {
    def evaluate(record: Record) = record.severity.ordinal
  }

}
