package ru.dimsuz.way

data class Event(val name: Name, val payload: Any? = null) {
  @JvmInline
  value class Name(val value: String) {
    companion object {
      val BACK = Name("BACK")

      internal val DONE = Name("DONE")
      internal val INIT = Name("INIT")
    }
  }
}
