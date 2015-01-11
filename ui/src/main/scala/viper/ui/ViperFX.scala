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

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

class ViperFX extends Application {

  private val fxml = "/viper/ui/Viper.fxml"

  override def start(stage: Stage) {
    val loader = new FXMLLoader(getClass.getResource(fxml))
    val root: Parent = loader.load()

    stage.setTitle("Viper Log Viewer")
    stage.setScene(new Scene(root, 1000, 600))
    stage.show()

    begin(loader.getController())
  }

  def begin(controller: Controller): Unit = {
    ViperFX.controller = controller
  }

  override def stop(): Unit = {
    println("Closing")
    System.exit(-1) //todo Shouldn't need this
  }
}

object ViperFX {

  // todo Nasty but temporary hack while we have both Swing & JavaFX UIs
  private var controller: Controller = null
  def launch(): ViperUI = {
    new Thread(new Runnable {
      override def run(): Unit = {
        Application.launch(classOf[ViperFX])
      }
    }, "init").start()
    while (controller == null) {
      Thread.sleep(100)
    }
    controller
  }

  def main(args: Array[String]) {
    Application.launch(classOf[ViperFX])
  }

}
