package ru.dimsuz.way

open class ActionEnv<S : Any, A : Any>(
  val path: Path
) {
  var queuedEvents: MutableList<Event>? = null
    private set

  val args: A
    get() {
      TODO()
    }
  val state: S
    get() {
      TODO()
    }
  val event: Event?
    get() {
      TODO()
    }

  fun updateState(transform: (state: S) -> S) {
    TODO()
  }

  fun sendEvent(event: Event) {
    if (queuedEvents == null) {
      queuedEvents = mutableListOf()
    }
    queuedEvents!!.add(event)
  }
}
