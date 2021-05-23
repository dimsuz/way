package ru.dimsuz.way

@JvmInline
value class Event(val name: String) {
  companion object {
    val BACK = Event("BACK")
  }
}
