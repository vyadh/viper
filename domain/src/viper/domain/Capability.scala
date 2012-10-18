package viper.domain

sealed trait Capability
trait Movable extends Capability
trait Deletable extends Capability
trait Persistable extends Capability {
  val id: String
}
trait Levelable extends Capability {
  val severity: Severity
}
