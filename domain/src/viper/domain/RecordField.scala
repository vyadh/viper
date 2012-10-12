package viper.domain

class RecordField(
      val name: String,
      /** A function to create a value, taking a class that extends from content. */
      val value: Record => AnyRef
)
