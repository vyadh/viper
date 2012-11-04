package viper.ui

import javax.swing._
import java.awt.BorderLayout
import ca.odell.glazedlists._
import viper.domain._
import collection.mutable
import collection.JavaConversions.seqAsJavaList
import collection.JavaConversions.collectionAsScalaIterable
import viper.util.EQ

class ViperFrame(val name: String) extends JFrame(name) with UI with ViperComponents with RecordFiltering {

  /** The currently active subscriptions, as shown in the left-side list. */
  val subscriberEventList = new BasicEventList[Subscribed]
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

  private def createMainComponents(subscriberEventList: EventList[Subscribed]): MainComponents = {
    val subscribedList = new SubscribedList(subscriberEventList, changeTo)
    val severitySlider = new SeveritySlider(updateCurrentSeverity) { setEnabled(false) }
    val searchBox = new SearchBox(search) { setEnabled(false) }
    val preview = new TextArea { setEditable(false) }
    val table = new RecordTable(select(preview), tableColumnWidthChanged)

    new MainComponents(subscribedList, severitySlider, searchBox, table, preview)
  }

  private def initLayout(components: MainComponents) {
    val content = getContentPane

    // Components
    val toolBar = createToolBar(components.severitySlider, components.searchBox)
    val subscriptionScroll = new ScrollPane(components.subscriptionList)
    val tableWithPreview = new VerticalSplitPane(new ScrollPane(components.table), new ScrollPane(components.preview))
    val main = new HorizontalSplitPane(subscriptionScroll, tableWithPreview)

    // Prefs
    registerPrefs("table-preview", tableWithPreview)
    registerPrefs("main", main)

    // Popup table menu
    components.table.setComponentPopupMenu(new JPopupMenu {
      add(Actions.markRead)
      add(Actions.markUnread)
      add(Actions.delete)
    })

    // Borders
    val border = 3
    toolBar.setBorder(BorderFactory.createCompoundBorder(toolBar.getBorder, new EmptyBorder(border)))
    components.subscriptionList.setBorder(new EmptyBorder(border))

    // Layout
    content.add(toolBar, BorderLayout.NORTH)
    content.add(main, BorderLayout.CENTER)
  }

  private def createToolBar(severityLevel: SeveritySlider, searchBox: SearchBox) = new ToolBar {
    add(Actions.markRead)
    add(Actions.markUnread)
    add(Actions.delete)
    addFiller()
    add(severityLevel.label)
    add(severityLevel)
    addSeparator()
    add(new JLabel("Search "))
    add(searchBox)
  }


  // Actions

  private def tableColumnWidthChanged(table: JTable) {
    storeColumnWidths(activeView.subscription.name, table)
  }

  private def changeTo(subscribed: Subscribed) {
    val view = viewObjectsBySubscriber(subscribed.subscriber)

    main.severitySlider.install(view.severitied, view.currentSeverityFilter)
    main.searchBox.setText(view.currentSearchFilter)
    main.table.install(view.filtered, view.sorted, view.format)

    // Now we know a subscription has been selected, we can enable a few things
    main.severitySlider.setEnabled(true)
    main.searchBox.setEnabled(true)

    // First column is always the Record object, so don't display it
    main.table.hideColumn(0)

    // Restore previous widths (if any exist)
    restoreColumnWidths(view.subscription.name, main.table)

    // Any current preview doesn't make much sense now
    main.preview.setText("")
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

  private def select(preview: JTextArea)(selected: EventList[Record]) {
    if (!selected.isEmpty) {
      val first = selected.get(0)
      preview.setText(first.body)

      // Mark as isRead if just one selected
      if (selected.size == 1) {
        markRead(first, activeView.subscribed)
        main.subscriptionList.repaint()

        // Repaint row, otherwise it doesn't look like it has been marked read
        selected.set(0, selected.get(0))
      }
    }
  }

  private def markAllRead(read: Boolean = true)() { // Curry for use by action
    // Mark as new status, and rely on deselection to repaint the list
    // This is much faster than updates through EventList
    for (record <- main.table.selected) {
      markReadUnread(record, activeView.subscribed, read)
    }

    // Deselect, which also covers repainting all the rows
    main.table.getSelectionModel.clearSelection()

    // Repaint subscriber list with new counts
    main.subscriptionList.repaint()
  }

  private def deleteItem() {
    // Need to mark items as read to ensure subscription read counts updated
    for (record <- main.table.selected) {
      markReadUnread(record, activeView.subscribed, true)
    }

    main.table.deleteSelected(activeView.filtered, activeView.data)

    // Repaint subscriber list with new unread counts
    main.subscriptionList.repaint()
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
    for (subscriber <- viewObjectsBySubscriber.keys) {
      removeSubscription(subscriber)
    }
  }

  def addSubscription(subscription: Subscription) {
    val vos = viewObjects(subscription)
    viewObjectsBySubscriber.put(subscription.subscriber, vos)
    subscriberEventList.add(vos.subscribed)
  }

  private def viewObjects(subscription: Subscription): ViewObjects = {
    val format = new RecordTableFormat(subscription.prototype)

    val subscribed = new Subscribed(subscription.subscriber)

    val data = subscribe(subscription, subscribed)
    val severitied = thresholdList(data)
    val sorted = sortedList(severitied)
    val (filterer, filtered) = filteredList(subscription.prototype, sorted)

    ViewObjects(subscription, format, data, severitied, sorted, filtered, filterer, subscribed)
  }

  private def subscribe(subscription: Subscription, subscribed: Subscribed): EventList[Record] = {
    val result = new BasicEventList[Record]()
    // Subscribe to events by adding them to the event list as they come in
    subscription.deliver(records => EQ.later {
      result.addAll(records)
      subscribed.added(records)
      main.subscriptionList.repaint()
    })
    result
  }


  // Util functions

  private def activeSubscriber: Subscriber = main.subscriptionList.selected.get(0).subscriber

  private def activeView: ViewObjects = {
    val opt = viewObjectsBySubscriber.get(activeSubscriber)
    if (opt.isEmpty) {
      throw new IllegalStateException("No active view")
    }
    opt.get
  }

  private def markRead(record: Record, subscribed: Subscribed) {
    markReadUnread(record, subscribed, newRead=true)
  }

  private def markReadUnread(record: Record, subscribed: Subscribed, newRead: Boolean) {
    record match {
      // Set the new status and forward on if it has changed
      case r: Readable if (newRead ^ r.read) => {
        r.read = newRead
        if (newRead) subscribed.read(record, !r.read)
        else subscribed.unread(record, !r.read)
      }
      case _ =>
    }
  }


  // Actions

  object Actions {
    val markRead = new SimpleAction("Mark Read", markAllRead(true))
    val markUnread = new SimpleAction("Mark Unread", markAllRead(false))
    val delete = new SimpleAction("Delete", deleteItem)
  }

}
