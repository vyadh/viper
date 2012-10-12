package viper.domain

sealed trait Capability
trait Movable extends Capability
trait Deletable extends Capability
trait Persistable extends Capability
trait Levelable extends Capability
