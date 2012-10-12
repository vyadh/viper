package viper.domain

/**
 * Described the fields on a record, ultimately used to layout a table.
 * @tparam T a prototype has fields that are specific to the record implementation
 */
trait RecordPrototype {

  def fields: Array[RecordField]

}
