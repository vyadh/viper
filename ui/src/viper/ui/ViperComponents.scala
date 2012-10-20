package viper.ui

import ca.odell.glazedlists.{FilterList, SortedList, EventList}
import viper.domain.{Subscription, Record, Subscriber}
import javax.swing.{JLabel, JTextArea}
import java.awt.Dimension
import ca.odell.glazedlists.matchers.TextMatcherEditor

trait ViperComponents extends UIComponents {

  case class MainComponents(
    subscriptionList: ListPanel[Subscriber],
    searchBox: SearchBox,
    table: FilterableSortableTable[Record],
    preview: JTextArea
  )

  case class ViewObjects(
    subscription: Subscription,
    format: RecordTableFormat,
    data: EventList[Record],
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

  class SeverityLevel extends Slider(0, 5) {
    setMaximumSize(new Dimension(80, 20))
    val label = new JLabel("{Level}")
  }

}
