package ru.dimsuz.way

open class TransitionEnv<S : Any, A : Any, R : Any>(path: Path) : ActionEnv<S, A>(path) {

  internal sealed class Destination {
    data class RelativeNode(val key: NodeKey) : Destination()
    data class Path(val path: ru.dimsuz.way.Path) : Destination()
  }

  internal var destination: Destination? = null
    private set

  fun navigateTo(key: NodeKey) {
    destination = Destination.RelativeNode(key)
  }

  fun navigateTo(path: Path) {
    destination = Destination.Path(path)
  }

  fun finish(result: R) {
    // TODO make it so that calling this results in sending `Event.DONE`
    //  (for example by internally adding action returning listOf(Event.DONE))
    TODO()
  }
}
