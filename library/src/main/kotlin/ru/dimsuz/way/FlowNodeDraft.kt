package ru.dimsuz.way

internal data class FlowNodeDraft<S : Any, A : Any, R : Any>(
  var initial: NodeId? = null,
)
