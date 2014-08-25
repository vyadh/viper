/*
 * Copyright 2012-2014 Kieron Wilkinson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
