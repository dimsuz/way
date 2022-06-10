package ru.dimsuz.way

open class ActionEnv<S : Any, A : Any>(
  val path: Path,
  val event: Event,
  val state: S,
) {
  internal var queuedEvents: MutableList<Event>? = null
    private set

  internal var updatedState: S = state
    private set

  val args: A
    get() {
      TODO()
    }

  fun updateState(transform: (state: S) -> S) {
    updatedState = transform(updatedState)
  }

  fun sendEvent(event: Event) {
    if (queuedEvents == null) {
      queuedEvents = mutableListOf()
    }
    queuedEvents!!.add(event)
  }
}
