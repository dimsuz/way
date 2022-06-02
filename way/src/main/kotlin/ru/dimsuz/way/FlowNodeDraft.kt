package ru.dimsuz.way

internal data class FlowNodeDraft<S : Any, A : Any, R : Any>(
  var initial: NodeKey? = null,
  var eventTransitions: MutableMap<Event.Name, (TransitionEnv<*, *, *>) -> Unit> = mutableMapOf(),
  val screenBuildActions: MutableMap<NodeKey, (ScreenNodeBuilder<S, A, R>) -> ScreenNode> = mutableMapOf(),
  val flowBuildActions: MutableMap<NodeKey, (SubFlowBuilder<S, A, R, *>) -> FlowNode<*, *, *>> = mutableMapOf(),
  var onEntry: ((ActionEnv<*, *>) -> Unit)? = null,
  var onExit: ((ActionEnv<*, *>) -> Unit)? = null
)