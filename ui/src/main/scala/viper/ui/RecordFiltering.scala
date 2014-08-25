package viper.ui

import java.util.concurrent.LinkedBlockingQueue

import ca.odell.glazedlists.matchers.TextMatcherEditor
import viper.domain.Record

trait RecordFiltering {

  /** Cache of previous command, to ensure the new filter operation is necessary. */
  var previous = ""
  
  /** Command queue for filtering commands. */
  val filterCommands = new LinkedBlockingQueue[AnyRef]

  /** Command to do filtering. */
  private case class Filter(
        expression: String,
        filterer: TextMatcherEditor[Record],
        whenDone: () => Unit)

  /** Object used to shut down actor. */
  private case object ExitFiltering


  def filter(expression: String, filterer: TextMatcherEditor[Record], whenDone: () => Unit) {
    filterCommands.add(new Filter(expression, filterer, whenDone))
  }

  def closeFiltering() {
    filterCommands.add(ExitFiltering)
  }


  /** Actor helps avoid blocking UI thread when filtering large amount of data. */
  private val filterThread = new Thread("filter") {
    override def run(): Unit = {
      var finished = false
      while (!finished) {
        filterCommands.take() match {
          case command: Filter => {
            // Only execute commands if there is nothing else in the queue
            // This has the disadvantage that it will always do at least two work items (first and last)
            if (filterCommands.isEmpty) {
              filterInThread(command)
            }
          }
          case ExitFiltering => finished = true
        }
      }
    }
  }.start()

  private def filterInThread(command: Filter) {
    import TextMatcherEditor._
    val filters = command.filterer.getMode match {
      case CONTAINS => command.expression.split("[ \t]")
      case m if Set(STARTS_WITH, REGULAR_EXPRESSION, EXACT).contains(m) => Array(command.expression)
    }

    command.filterer.setFilterText(filters)
    command.whenDone()
  }

}
