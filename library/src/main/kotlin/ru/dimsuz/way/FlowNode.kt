package ru.dimsuz.way

data class FlowNode<S : Any, A : Any, R : Any>(
  val initial: NodeId,
  val children: Map<NodeId, Node>,
  override val eventTransitions: Map<Event, TransitionEnv<*, *, *>>
) : Node()
