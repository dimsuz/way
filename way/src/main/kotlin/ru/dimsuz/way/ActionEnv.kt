package ru.dimsuz.way

open class ActionEnv<S : Any, A : Any> internal constructor(
  val path: Path,
  val event: Event,
  val readState: () -> S,
) {
  internal var queuedEvents: MutableList<Event>? = null
    private set

  internal var updatedState: S? = null
    private set

  val args: A
    get() {
      TODO()
    }

  val state: S
    get() = readState()

  fun updateState(transform: (state: S) -> S) {
    updatedState = transform(updatedState ?: state)
  }

  fun sendEvent(event: Event) {
    if (queuedEvents == null) {
      queuedEvents = mutableListOf()
    }
    queuedEvents!!.add(event)
  }
}
