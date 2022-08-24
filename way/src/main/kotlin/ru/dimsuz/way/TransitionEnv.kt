package ru.dimsuz.way

open class TransitionEnv<S : Any, R : Any> internal constructor(
  val event: Event,
) {
  internal sealed class Destination {
    data class RelativeNode(val key: NodeKey) : Destination()
    data class Path(val path: ru.dimsuz.way.Path) : Destination()
  }

  internal var destination: Destination? = null
    private set

  internal var finishResult: R? = null

  internal var queuedEvents: MutableList<Event>? = null
    private set

  fun navigateTo(key: NodeKey) {
    destination = Destination.RelativeNode(key)
  }

  fun navigateTo(path: Path) {
    destination = Destination.Path(path)
  }

  fun finish(result: R) {
    finishResult = result
  }

  fun sendEvent(event: Event) {
    if (queuedEvents == null) {
      queuedEvents = mutableListOf()
    }
    queuedEvents!!.add(event)
  }
}
