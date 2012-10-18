package viper.domain

sealed class Severity(val ordinal: Int) extends Comparable[Severity] {
  val name = getClass.getSimpleName
  override def toString = name
  def compareTo(s: Severity) = ordinal - s.ordinal
}

case object Severe  extends Severity(0)
case object Warning extends Severity(1)
case object Info    extends Severity(2)
case object Config  extends Severity(3)
case object Fine    extends Severity(4)
case object Finer   extends Severity(5)
case object Finest  extends Severity(6)
