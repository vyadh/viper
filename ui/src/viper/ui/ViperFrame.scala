package viper.ui

import javax.swing._
import java.awt.{Color, Component, BorderLayout}
import ca.odell.glazedlists._
import table.TableCellRenderer
import viper.domain.{RecordPrototype, Record, Subscriber, Subscription}
import matchers.TextMatcherEditor
import java.util
import collection.mutable
import collection.JavaConversions.seqAsJavaList
import viper.util.{Colours, EQ}

class ViperFrame(val name: String) extends JFrame(name) with UI with Filtering {

  /** The currently active subscriptions, as shown in the left-side list. */
  val subscriberEventList = new BasicEventList[Subscriber]
  /** Objects related to a subscription, keyed by the subscriber object. */
  val viewObjectsBySubscriber = new mutable.HashMap[Subscriber, ViewObjects]

  val main = init()


  def close() {
    closeFiltering()
    removeSubscriptions()
  }


  // Useful classes

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


  // Component construction and Layout

  def init(): MainComponents = {
    val main = createMainComponents(subscriberEventList)
    initLayout(main)
    main
  }

  def createMainComponents(subscriberEventList: EventList[Subscriber]): MainComponents = {
    val subscriberList = new ListPanel[Subscriber](subscriberEventList, changeTo)
    val searchBox = new SearchBox(filter) { setEnabled(false) }
    val table = new RecordTable
    val preview = new JTextArea

    subscriberList.setCellRenderer(new SubscriberCellRenderer)

    new MainComponents(subscriberList, searchBox, table, preview)
  }

  def initLayout(components: MainComponents) {
    val content = getContentPane

    // Components
    val toolBar = createToolBar(components.searchBox)
    val subscriptionScroll = new ScrollPane(components.subscriptionList)
    val tableWithPreview = new VerticalSplitPane(new ScrollPane(components.table), components.preview)
    val main = new HorizontalSplitPane(subscriptionScroll, tableWithPreview)

    // Borders
    val border = 3
    toolBar.setBorder(BorderFactory.createCompoundBorder(toolBar.getBorder, new EmptyBorder(border)))
    components.subscriptionList.setBorder(new EmptyBorder(border))

    // Layout
    content.add(toolBar, BorderLayout.NORTH)
    content.add(main, BorderLayout.CENTER)
  }

  def createToolBar(searchBox: SearchBox) = new ToolBar() {
    addFiller()
    add(new JLabel("Search "))
    add(searchBox)
  }


  // Actions

  def changeTo(subscriber: Subscriber) {
    val view = viewObjectsBySubscriber(subscriber)
    main.searchBox.restore(view.filter)
    main.table.install(view.filtered, view.sorted, view.format)
    main.searchBox.setEnabled(true)
    main.table.hideColumn(0) // First column is always record, so don't display it
  }

  def filter(expression: String) {
    val filterer = activeView.filterer
    def updateViewFilter() { activeView.filter = expression }
    filter(expression, filterer, updateViewFilter)
  }


  // Add/remove subscription

  def removeSubscription(subscriber: Subscriber) {
    val view = viewObjectsBySubscriber.remove(subscriber)
    view.map(_.subscription).foreach(_.stop())

    subscriberEventList.remove(subscriber)
    // todo and if it is the currently viewed subscription?
  }

  def removeSubscriptions() {
    val subscriptions = viewObjectsBySubscriber.values.map(_.subscription)
    subscriptions.foreach(_.stop())
  }

  def addSubscription(subscription: Subscription) {
    val vos = viewObjects(subscription)
    viewObjectsBySubscriber.put(subscription.subscriber, vos)
    subscriberEventList.add(subscription.subscriber)
  }

  def viewObjects(subscription: Subscription): ViewObjects = {
    val format = new RecordTableFormat(subscription.prototype)

    val data = subscribe(subscription)
    val sorted = sortedList(data)
    val (filterer, filtered) = filteredList(subscription.prototype, sorted)

    ViewObjects(subscription, format, data, sorted, filtered, filterer, "")
  }

  def subscribe(subscription: Subscription): EventList[Record] = {
    val result = new BasicEventList[Record]()
    // Subscribe to events by adding them to the event list as they come in
    subscription.deliver(records => EQ.later { result.addAll(records) })
    result
  }

  def sortedList(eventList: EventList[Record]) = new SortedList[Record](eventList, null)

  def filteredList(prototype: RecordPrototype, eventList: SortedList[Record]):
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


  // Util functions

  def activeSubscriber: Subscriber = main.subscriptionList.selected.get(0)

  def activeView: ViewObjects = {
    val opt = viewObjectsBySubscriber.get(activeSubscriber)
    if (opt.isEmpty) {
      throw new IllegalStateException("No active view")
    }
    opt.get
  }


  // Record-specific components

  class RecordTable extends FilterableSortableTable[Record] {
    override def prepareRenderer(renderer: TableCellRenderer, row: Int, column: Int): Component = {
      val c = super.prepareRenderer(renderer, row, column)
      val r = getModel.getValueAt(row, 0).asInstanceOf[Record]

      if (isRowSelected(row)) {
        c.setForeground(Colours.blend(r.severity.colour, Color.white, 150))
      } else {
        c.setForeground(r.severity.colour)
      }
      c
    }
  }

}
