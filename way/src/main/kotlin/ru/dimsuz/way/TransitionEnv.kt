package ru.dimsuz.way

open class TransitionEnv<S : Any, A : Any, R : Any>(path: Path, event: Event) : ActionEnv<S, A>(path, event) {

  internal sealed class Destination {
    data class RelativeNode(val key: NodeKey) : Destination()
    data class Path(val path: ru.dimsuz.way.Path) : Destination()
  }

  internal var destination: Destination? = null
    private set

  internal var finishResult: R? = null

  fun navigateTo(key: NodeKey) {
    destination = Destination.RelativeNode(key)
  }

  fun navigateTo(path: Path) {
    destination = Destination.Path(path)
  }

  fun finish(result: R) {
    finishResult = result
  }
}
