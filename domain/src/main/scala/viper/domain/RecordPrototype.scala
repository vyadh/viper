package viper.domain

/**
 * Described the fields on a record, ultimately used to layout a table.
 * @tparam T a prototype has fields that are specific to the record implementation
 */
trait RecordPrototype {

  /** Fields that are expected to be shown as columns on the view table. */
  def fields: Array[RecordField]

  /** Default sorting fields, and whether to reverse the sort order (true) or not (false) */
  def defaultSort: List[(RecordField, Boolean)]

}
