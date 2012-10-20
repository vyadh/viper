package viper.ui

import ca.odell.glazedlists.matchers.TextMatcherEditor
import viper.domain.Record
import actors.Actor._

trait RecordFiltering {

  /** Command to do filtering. */
  private case class Filter(
        expression: String,
        filterer: TextMatcherEditor[Record],
        whenDone: () => Unit)

  /** Object used to shut down actor. */
  private case object ExitFiltering


  def filter(expression: String, filterer: TextMatcherEditor[Record], whenDone: () => Unit) {
    filterActor ! new Filter(expression, filterer, whenDone)
  }

  def closeFiltering() {
    filterActor ! ExitFiltering
  }


  /** Actor helps avoid blocking UI thread when filtering large amount of data. */
  private val filterActor = actor {
    loop {
      react {
        case command: Filter => filterInThread(command)
        case ExitFiltering => exit()
      }
    }
  }

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
