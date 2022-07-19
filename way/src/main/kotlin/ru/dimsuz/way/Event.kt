package ru.dimsuz.way

import java.util.UUID

data class Event(val name: Name, val payload: Any? = null) {

  companion object {
    val BACK = Event(Name.BACK, payload = null)
  }

  @JvmInline
  value class Name(val value: String) {
    companion object {
      val BACK = Name("BACK")

      internal val INIT = Name("INIT")

      private const val DONE_PREFIX = "FINAL_NODE_DONE"

      fun buildDone(): Event.Name {
        return Event.Name("${DONE_PREFIX}_${UUID.randomUUID()}")
      }
    }

    fun isDone(): Boolean {
      return this.value.startsWith(DONE_PREFIX)
    }
  }
}
