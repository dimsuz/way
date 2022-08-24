package ru.dimsuz.way

internal data class FlowNodeDraft<S : Any, R : Any>(
  var initial: NodeKey? = null,
  val doneEventName: Event.Name = Event.Name.buildDone(),
  var eventTransitions: MutableMap<Event.Name, (TransitionEnv<*, *>) -> Unit> = mutableMapOf(),
  val screenBuildActions: MutableMap<NodeKey, (ScreenNodeBuilder<S, R>) -> ScreenNode> = mutableMapOf(),
  val finalNodeBuildActions: MutableMap<NodeKey, (FinalNodeBuilder<S, R>) -> FinalNode> = mutableMapOf(),
  val flowBuildActions: MutableMap<NodeKey, (SubFlowBuilder<S, R, *>) -> FlowNode<*, *>> = mutableMapOf(),
  var onEntry: ((ActionEnv<*>) -> Unit)? = null,
  var onExit: ((ActionEnv<*>) -> Unit)? = null
)
