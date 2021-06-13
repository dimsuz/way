package ru.dimsuz.way

internal data class FlowNodeDraft<S : Any, A : Any, R : Any>(
  var initial: NodeId? = null,
  val screenBuildActions: MutableMap<NodeId, (ScreenNodeBuilder<S, A, R>) -> ScreenNode> = mutableMapOf(),
)
