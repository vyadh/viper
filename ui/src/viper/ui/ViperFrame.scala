package viper.ui

import javax.swing._
import java.awt.BorderLayout
import ca.odell.glazedlists._
import viper.domain.{RecordPrototype, Record, Subscriber, Subscription}
import matchers.TextMatcherEditor
import java.util
import collection.mutable
import scala.actors.Actor._

class ViperFrame(val name: String) extends JFrame(name) with UI {

  /** The currently active subscriptions, as shown in the left-side list. */
  val subscriberEventList = new BasicEventList[Subscriber]
  /** Objects related to a subscription, keyed by the subscriber object. */
  val viewObjectsBySubscriber = new mutable.HashMap[Subscriber, ViewObjects]
  /** Object used to shut down actors. */
  case object Exit

  val main = createMainComponents(subscriberEventList)
  layoutComponents()


  def close() {
    filterActor ! Exit
  }

  class MainComponents(
    val subscriptionList: ListPanel[Subscriber],
    val views: CardPanel,
    val searchBox: SearchBox
  )

  def createMainComponents(subscriberEventList: EventList[Subscriber]): MainComponents = {
    val subscriberList = new ListPanel[Subscriber](subscriberEventList, flipTo)
    val views = new CardPanel
    val searchBox = new SearchBox(expression => filterActor ! expression)

    new MainComponents(subscriberList, views, searchBox)
  }

  def layoutComponents() {
    val content = getContentPane
    content.add(createToolBar, BorderLayout.NORTH)
    content.add(new ScrollPane(main.subscriptionList), BorderLayout.WEST)
    content.add(main.views, BorderLayout.CENTER)
  }

  def createToolBar = new ToolBar() {
    addFiller()
    add(new JLabel("Search "))
    add(main.searchBox)
  }


  def addSubscription(subscription: Subscription) {
    val vos = viewObjects(subscription)
    val v = view(vos)
    main.views.add(subscription.ref, v)
    viewObjectsBySubscriber.put(subscription.subscriber, vos)
    subscriberEventList.add(subscription.subscriber)
  }

  def removeSubscription(subscriber: Subscriber) {
    main.views.remove(subscriber.ref)
    viewObjectsBySubscriber.remove(subscriber)
    subscriberEventList.remove(subscriber)
  }

  def viewObjects(subscription: Subscription): ViewObjects = {
    val format = new RecordTableFormat(subscription.prototype)

    val data = subscribe(subscription)
    val sorted = sortedList(data)
    val (filterer, filtered) = filteredList(subscription.prototype, sorted)
    val table = new FilterableSortableTable(filtered, sorted, format)

    val preview = new JTextArea()

    ViewObjects(subscription, data, table, preview, filterer)
  }

  case class ViewObjects(
        subscription: Subscription,
        data: EventList[Record],
        table: JTable,
        preview: JTextArea,
        filterer: TextMatcherEditor[Record]
  )

  def view(viewObjs: ViewObjects): JComponent = {
    new VerticalSplitPane(new ScrollPane(viewObjs.table), viewObjs.preview)
  }

  def flipTo(subscriber: Subscriber) {
    selectFilter(subscriber)

    main.views.show(subscriber.ref)
  }

  def selectFilter(subscriber: Subscriber) {
    main.searchBox.setText("")
  }

  def subscribe(subscription: Subscription): EventList[Record] = {
    val result = new BasicEventList[Record]()

    for (r <- subscription.testData) { // todo testdata
      result.add(r)
    }

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

  /** Actor helps avoid blocking UI thread when filtering large amount of data. */
  val filterActor = actor {
    loop {
      react {
        case expression: String => filter(expression)
        case Exit => exit()
      }
    }
  }

  def filter(expression: String) {
    val filterer = activeViewObjects.filterer

    import TextMatcherEditor._
    val filters = filterer.getMode match {
      case CONTAINS => expression.split("[ \t]")
      case m if Set(STARTS_WITH, REGULAR_EXPRESSION, EXACT).contains(m) => Array(expression)
    }

    filterer.setFilterText(filters) // todo need to restore when we revert to this view
  }

  def activeSubscriber: Subscriber = {
    main.subscriptionList.getSelectedValue
  }

  def activeViewObjects: ViewObjects = {
    val opt = viewObjectsBySubscriber.get(activeSubscriber)
    if (opt.isEmpty) {
      throw new IllegalStateException("No active view")
    }
    opt.get
  }

}
