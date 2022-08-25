package ru.dimsuz.way

open class ActionEnv<S : Any> internal constructor(
  val event: Event,
  val readState: () -> S,
) {
  // TODO make internal val List<>, hide mutability
  internal var queuedEvents: MutableList<Event>? = null
    private set

  internal var updatedState: S? = null
    private set

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
