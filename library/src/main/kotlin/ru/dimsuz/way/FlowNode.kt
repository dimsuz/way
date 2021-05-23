package ru.dimsuz.way

data class FlowNode<S : Any, A : Any, R : Any>(
  val initial: NodeId,
)
