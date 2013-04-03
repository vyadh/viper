package viper.util

import java.util.{TimerTask, Timer}

/**
 * Utility to perform an action if a timeout is reached, or if we can fire it manually for it to be done explicitly.
 *
 * @param timeout in millis
 * @param action work to do on timeout, or when another action is staged
 */
class TimeoutTask(timeout: Int)(action: => Unit) {

  val timer = new Timer()
  var task = createTask

  /** Indicate new action is staged for timeout. Any existing previous action will fired. */
  def stage() {
    fireAction()
    schedule()
  }

  /** Indicate work is in progress, and we should wait a further timeout value before firing the action. */
  def delay() {
    // If we managed to cancel the task, re-schedule it to run after the timeout again
    ifCancelled {
      schedule()
    }
  }

  /** Close any resources associated with this object. */
  def close() {
    timer.cancel()
  }


  /** Don't wait for the timeout, fire action now. */
  private def fireAction() {
    // If we managed to cancel the task, run the action (avoids running it twice)
    ifCancelled {
      action
    }
  }

  private def schedule() {
    task = createTask
    timer.schedule(task, timeout)
  }

  private def ifCancelled(doThis: => Unit) {
    if (cancel()) {
      doThis
    }
  }

  private def createTask = new TimerTask {
    def run() {
      action
    }
  }

  private def cancel(): Boolean = {
    val cancelled = task.cancel()
    cancelled
  }

}
