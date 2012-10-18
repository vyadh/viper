package viper.domain

import java.awt.Color

sealed class Severity(val ordinal: Int, val colour: Color) extends Comparable[Severity] {
  val name = getClass.getSimpleName
  override def toString = name
  def compareTo(s: Severity) = ordinal - s.ordinal
}

case object Severe  extends Severity(0, new Color(160, 40, 40))
case object Warning extends Severity(1, new Color(140, 90, 0))
case object Info    extends Severity(2, Color.black)
case object Config  extends Severity(3, new Color(0, 0, 140))
case object Fine    extends Severity(4, Color.darkGray)
case object Finer   extends Severity(5, Color.gray)
case object Finest  extends Severity(6, Color.lightGray)

object Severities {
  val values = Seq(Severe, Warning, Info, Config, Fine, Finer, Finest)
  val count = values.size
}
