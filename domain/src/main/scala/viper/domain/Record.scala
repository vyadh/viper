package viper.domain

trait Record {

  val id: String
  val severity: Severity
  def body: String

}
