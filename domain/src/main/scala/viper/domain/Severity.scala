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

import java.awt.Color

sealed class Severity(val ordinal: Int, val colour: Color) extends Comparable[Severity] {
  val name = getClass.getSimpleName.substring(0, getClass.getSimpleName.size-1)
  override def toString = name
  def compareTo(s: Severity) = ordinal - s.ordinal
}

case object Severe  extends Severity(6, new Color(160, 40, 40))
case object Warning extends Severity(5, new Color(140, 90, 0))
case object Info    extends Severity(4, Color.black)
case object Config  extends Severity(3, new Color(0, 0, 140))
case object Fine    extends Severity(2, Color.darkGray)
case object Finer   extends Severity(1, Color.gray)
case object Finest  extends Severity(0, Color.lightGray)

object Severities {
  val values = Seq(Severe, Warning, Info, Config, Fine, Finer, Finest)
  val min = Finest
  val max = Severe
  val all = Finest
  val count = values.size
}
