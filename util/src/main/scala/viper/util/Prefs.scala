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

import java.util.prefs.Preferences
import java.awt.{Point, Dimension}
import javax.swing.{JFrame, JSplitPane}
import collection.mutable

trait Prefs {

  private lazy val prefs = Preferences.userNodeForPackage(getClass)

  private val saving  = mutable.ListBuffer[() => Unit]()
  private val loading = mutable.ListBuffer[() => Unit]()


  def restorePrefs() {
    loading.foreach(_())
  }

  def storePrefs() {
    saving.foreach(_())
  }


  def registerPrefs(name: String, component: JFrame, defaultSize: Dimension) {
    val key = name + ".frame"

    loading += { () =>
      component.setSize(restoreDimension(key).getOrElse(defaultSize))

      restorePoint(key) match {
        case Some(p) => component.setLocation(p)
        case None    => component.setLocationRelativeTo(null)
      }
    }

    saving += { () =>
      store(key, component.getSize)
      store(key, component.getLocation)
    }
  }

  def registerPrefs(name: String, component: JSplitPane) {
    val key = name + ".divider"

    loading += { () =>
      val value = prefs.getInt(key, -1)
      if (value != -1) {
        component.setDividerLocation(value)
      }
    }

    saving += { () =>
      prefs.putInt(key, component.getDividerLocation)
    }
  }


  private def store(name: String, value: Dimension) {
    prefs.putInt(name + ".width", value.width)
    prefs.putInt(name + ".height", value.height)
  }

  private def restoreDimension(name: String): Option[Dimension] = {
    val width = prefs.getInt(name + ".width", -1)
    val height = prefs.getInt(name + ".height", -1)

    if (width == -1 || height == -1) None
    else Some(new Dimension(width, height))
  }

  private def store(name: String, value: Point) {
    prefs.putInt(name + ".x", value.x)
    prefs.putInt(name + ".y", value.y)
  }

  private def restorePoint(name: String): Option[Point] = {
    val x = prefs.getInt(name + ".x", -1)
    val y = prefs.getInt(name + ".y", -1)

    if (x == -1 || y == -1) None
    else Some(new Point(x, y))
  }

}
