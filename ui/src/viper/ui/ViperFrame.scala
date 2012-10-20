package viper.ui

import javax.swing._
import java.awt.BorderLayout
import ca.odell.glazedlists._
import viper.domain.{Severity, Record, Subscriber, Subscription}
import collection.mutable
import collection.JavaConversions.seqAsJavaList
import viper.util.EQ

class ViperFrame(val name: String) extends JFrame(name) with UI with ViperComponents with RecordFiltering {

  /** The currently active subscriptions, as shown in the left-side list. */
  val subscriberEventList = new BasicEventList[Subscriber]
  /** Objects related to a subscription, keyed by the subscriber object. */
  val viewObjectsBySubscriber = new mutable.HashMap[Subscriber, ViewObjects]

  val main = init()


  def close() {
    closeFiltering()
    removeSubscriptions()
  }


  // Component construction and Layout

  private def init(): MainComponents = {
    val main = createMainComponents(subscriberEventList)
    initLayout(main)
    main
  }

  private def createMainComponents(subscriberEventList: EventList[Subscriber]): MainComponents = {
    val subscriberList = new SubscriberList(subscriberEventList, changeTo)
    val severitySlider = new SeveritySlider(updateCurrentSeverity) { setEnabled(false) }
    val searchBox = new SearchBox(search) { setEnabled(false) }
    val preview = new JTextArea { setEditable(false) }
    val table = new RecordTable(preview)

    new MainComponents(subscriberList, severitySlider, searchBox, table, preview)
  }

  private def initLayout(components: MainComponents) {
    val content = getContentPane

    // Components
    val toolBar = createToolBar(components.severitySlider, components.searchBox)
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

  private def createToolBar(severityLevel: SeveritySlider, searchBox: SearchBox) = new ToolBar {
    addFiller()
    add(severityLevel.label)
    add(severityLevel)
    addSeparator()
    add(new JLabel("Search "))
    add(searchBox)
  }


  // Actions

  private def changeTo(subscriber: Subscriber) {
    val view = viewObjectsBySubscriber(subscriber)

    main.severitySlider.install(view.severitied, view.currentSeverityFilter)
    main.searchBox.setText(view.currentSearchFilter)
    main.table.install(view.filtered, view.sorted, view.format)

    main.severitySlider.setEnabled(true)
    main.searchBox.setEnabled(true)

    main.table.hideColumn(0) // First column is always record, so don't display it
  }

  private def search(expression: String) {
    val filterer = activeView.filterer
    def updateCurrentSearch() { activeView.currentSearchFilter = expression }
    filter(expression, filterer, updateCurrentSearch)
  }

  /** Store the current severity on the active view if it is changed so we can restore later. */
  private def updateCurrentSeverity(severity: Severity) {
    activeView.currentSeverityFilter = severity
  }


  // Add/remove subscription

  def removeSubscription(subscriber: Subscriber) {
    val view = viewObjectsBySubscriber.remove(subscriber)
    for (v <- view) {
      v.subscription.stop()
      v.eventLists.foreach { _.dispose() }
    }

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

  private def viewObjects(subscription: Subscription): ViewObjects = {
    val format = new RecordTableFormat(subscription.prototype)

    val data = subscribe(subscription)

    val severitied = thresholdList(data)
    val sorted = sortedList(severitied)
    val (filterer, filtered) = filteredList(subscription.prototype, sorted)

    ViewObjects(subscription, format, data, severitied, sorted, filtered, filterer)
  }

  private def subscribe(subscription: Subscription): EventList[Record] = {
    val result = new BasicEventList[Record]()
    // Subscribe to events by adding them to the event list as they come in
    subscription.deliver(records => EQ.later { result.addAll(records) })
    result
  }


  // Util functions

  private def activeSubscriber: Subscriber = main.subscriptionList.selected.get(0)

  private def activeView: ViewObjects = {
    val opt = viewObjectsBySubscriber.get(activeSubscriber)
    if (opt.isEmpty) {
      throw new IllegalStateException("No active view")
    }
    opt.get
  }

}
