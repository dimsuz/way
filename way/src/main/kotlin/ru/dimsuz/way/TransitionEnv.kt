package ru.dimsuz.way

open class TransitionEnv<S : Any, R : Any> internal constructor(
  val event: Event,
  private val readState: () -> S,
) {

  internal sealed class Destination {
    data class RelativeNode(val key: NodeKey) : Destination()
    data class Path(val path: ru.dimsuz.way.Path) : Destination()
  }

  internal var destination: Destination? = null
    private set

  internal var finishResult: R? = null

  // TODO make internal val List<>, hide mutability
  internal var actions: MutableList<((ActionEnv<*>) -> Unit)>? = null
    private set

  val state: S
    get() = readState()

  fun navigateTo(key: NodeKey) {
    destination = Destination.RelativeNode(key)
  }

  fun navigateTo(path: Path) {
    destination = Destination.Path(path)
  }

  fun finish(result: R) {
    finishResult = result
  }

  fun action(body: ActionEnv<S>.() -> Unit) {
    if (actions == null) {
      actions = mutableListOf()
    }
    actions!!.add(body as (ActionEnv<*>) -> Unit)
  }
}
