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
package viper.domain

abstract case class Subscription(
      subscriber: Subscriber,
      prototype: RecordPrototype) {

  def ref = subscriber.ref
  def name = subscriber.name

  /**
   * Adds a new delivery point for messages.
   * @param to where to deliver messages.
   */
  def deliver(to: Seq[Record] => Unit)

  /**
   * Close the subscription, so no more events are delivered.
   * Duplicate calls have no effect.
   */
  def stop()

}
