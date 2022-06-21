package ru.dimsuz.way

class FlowNode<S : Any, A : Any, R : Any> internal constructor(
  val initial: NodeKey,
  val children: Map<NodeKey, Node>,
  state: S,
  override val eventTransitions: Map<Event.Name, (TransitionEnv<*, *, *>) -> Unit>,
  override val onEntry: ((ActionEnv<*, *>) -> Unit)?,
  override val onExit: ((ActionEnv<*, *>) -> Unit)?
) : Node {

  private var _state: S = state
  val state get() = _state

  internal fun setState(state: S) {
    _state = state
  }

  fun newBuilder(): FlowNodeBuilder<S, A, R> {
    return FlowNodeBuilder(this)
  }
}
