package viper.domain.log

sealed class Severity(val ordinal: Int) extends Comparable[Severity] {
  def compareTo(s: Severity) = ordinal - s.ordinal
}

case object Error   extends Severity(0)
case object Warning extends Severity(1)
case object Info    extends Severity(2)
case object Config  extends Severity(3)
case object Fine    extends Severity(4)
case object Finer   extends Severity(5)
case object Finest  extends Severity(6)
