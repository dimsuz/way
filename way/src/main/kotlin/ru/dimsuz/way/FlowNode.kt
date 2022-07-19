package ru.dimsuz.way

class FlowNode<S : Any, A : Any, R : Any> internal constructor(
  val initial: NodeKey,
  val children: Map<NodeKey, Node>,
  val doneEventName: Event.Name,
  state: S,
  override val eventTransitions: Map<Event.Name, (TransitionEnv<*, *, *>) -> Unit>,
  override val onEntry: ((ActionEnv<*, *>) -> Unit)?,
  override val onExit: ((ActionEnv<*, *>) -> Unit)?
) : Node {

  companion object {
    // There's always at least one final node defined on any flow: to support onFinish calls.
    // But there can be others added manually by user, using 'addFinalNode'
    internal val DEFAULT_FINAL_NODE_KEY = NodeKey("done")
  }

  private var _state: S = state
  val state get() = _state

  internal fun setState(state: S) {
    _state = state
  }

  fun newBuilder(): FlowNodeBuilder<S, A, R> {
    return FlowNodeBuilder(this)
  }
}
