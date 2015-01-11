/*
 * Copyright 2012-2015 Kieron Wilkinson.
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
package viper.ui

import java.net.URL
import java.util.{Objects, ResourceBundle}
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.event.ActionEvent
import javafx.fxml.{Initializable, FXML}
import javafx.scene.control._
import javafx.util.Callback
import TableColumn.CellDataFeatures

import viper.domain.{Subscriber, Record, Subscription}
import viper.util.EQ

import scala.collection.JavaConversions.{asJavaCollection, asScalaBuffer}

/**
 * JavaFX controller class.
 */
class Controller extends Initializable with ViperUI {

  @FXML protected var subscriptions: ListView[Subscription] = _
  @FXML protected var records: TableView[Record] = _
  @FXML protected var colTime: TableColumn[Record, String] = _
  @FXML protected var colSeq: TableColumn[Record, String] = _
  @FXML protected var colLevel: TableColumn[Record, String] = _
  @FXML protected var colMessage: TableColumn[Record, String] = _

  override def initialize(url: URL, resourceBundle: ResourceBundle): Unit = {
    checkFieldsInitialised()
    initSubscriptionsList()
    initRecordTable()
  }

  private def checkFieldsInitialised() = for {
    field <- classOf[Controller].getDeclaredFields
    if field.isAnnotationPresent(classOf[FXML])
  } {
    field.setAccessible(true)
    Objects.nonNull(field.get(this))
  }

  private def initSubscriptionsList() {
    subscriptions.setCellFactory(new SubscriptionsListCallback)
  }

  class SubscriptionsListCallback extends Callback[ListView[Subscription], ListCell[Subscription]] {
    override def call(view: ListView[Subscription]) = new SubscriptionListCell
  }

  class SubscriptionListCell extends ListCell[Subscription] {
    override def updateItem(item: Subscription, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      if (empty || item == null) {
        setText(null);
      } else {
        setText(item.name);
      }
    }
  }

  private def initRecordTable() {
    subscriptions.getItems.addListener(new ListChangeListener[Subscription] {
      override def onChanged(change: Change[_ <: Subscription]): Unit = {
        subscriptions.getSelectionModel.selectFirst()
        // Only needed on first added
        subscriptions.getItems.removeListener(this)
      }
    })
    subscriptions.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[Subscription] {
      override def changed(value: ObservableValue[_ <: Subscription], oldVal: Subscription, newVal: Subscription) {
        changeSubscription(newVal)
      }
    })
  }

  def changeSubscription(subscription: Subscription): Unit = {
    // Currently statically defined columns
    val conversion = subscription.prototype.fields.map(_.value.andThen(_.toString))

    colTime.setCellValueFactory(recordCellFactory(conversion(0)))
    colSeq.setCellValueFactory(recordCellFactory(conversion(1)))
    colLevel.setCellValueFactory(recordCellFactory(conversion(2)))
    colMessage.setCellValueFactory(recordCellFactory(conversion(3)))
  }

  def recordCellFactory(f: Record => String): Callback[CellDataFeatures[Record, String], ObservableValue[String]] = {
    new Callback[CellDataFeatures[Record, String], ObservableValue[String]] {
      def call(item: CellDataFeatures[Record, String]): ObservableValue[String] = {
        return new ReadOnlyObjectWrapper[String](f(item.getValue))
      }
    }
  }

  def delete(event: ActionEvent) {
    records.getItems.remove(records.getSelectionModel.getSelectedIndex)
  }

  override def addSubscription(subscription: Subscription): Unit = {
    EQ.laterFX {
      subscriptions.getItems.add(subscription)
      subscription.deliver(newRecords => EQ.laterFX {
        records.getItems.addAll(asJavaCollection(newRecords))
      })
    }
  }

  override def hasSubscriber(subscriber: Subscriber): Boolean = {
    return subscriptions.getItems
          .map(s => s.subscriber)
          .exists(s => s == subscriber)
  }

  override def focusOn(subscriber: Subscriber) = ???

  override def toFront() = ???

}
