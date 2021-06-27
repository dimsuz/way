package ru.dimsuz.way

data class FlowNode<S : Any, A : Any, R : Any>(
  val initial: NodeKey,
  val children: Map<NodeKey, Node>,
  override val eventTransitions: Map<Event, (TransitionEnv<*, *, *>) -> Unit>
) : Node {

  fun newBuilder(): FlowNodeBuilder<S, A, R> {
    return FlowNodeBuilder(this)
  }
}
