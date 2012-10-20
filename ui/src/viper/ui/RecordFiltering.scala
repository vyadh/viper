package viper.ui

import ca.odell.glazedlists.matchers.TextMatcherEditor
import viper.domain.Record

trait RecordFiltering {

  /** Cache of previous command, to ensure the new filter operation is necessary. */
  var previous = ""

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
  private val filterActor = new scala.actors.Actor {
    def act() {
      while (true) {
        receive {
          case command: Filter => {
            // Only execute commands if there is nothing else in the mailbox
            // This has the disadvantage that it will always do at least two work items (first and last)
            if (mailboxSize == 0) {
              filterInThread(command)
            }
          }
          case ExitFiltering => exit()
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
