package ru.dimsuz.way

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.toResultOr

class FlowNodeBuilder<S : Any, A : Any, R : Any> {
  private var draft = FlowNodeDraft<S, A, R>()

  enum class Error {
    MissingInitialNode
  }

  constructor()

  internal constructor(node: FlowNode<S, A, R>) {
    draft = FlowNodeDraft(
      initial = node.initial,
      screenBuildActions = node.children
        .filterValues { it is ScreenNode }
        .mapValuesTo(mutableMapOf()) { (_, node) -> { node as ScreenNode } },
      flowBuildActions = node.children
        .filterValues { it is FlowNode<*, *, *> }
        .mapValuesTo(mutableMapOf()) { (_, node) -> { node as FlowNode<*, *, *> } },
    )
  }

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun on(event: Event, transition: TransitionEnv<S, A, R>.() -> Unit): FlowNodeBuilder<S, A, R> {
    draft.eventTransitions[event] = transition as (TransitionEnv<*, *, *>) -> Unit
    return this
  }

  fun setInitial(key: NodeKey): FlowNodeBuilder<S, A, R> {
    draft.initial = key
    return this
  }

  fun setInitial(transition: TransitionEnv<S, A, R>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun addScreenNode(
    nodeKey: NodeKey,
    buildAction: (builder: ScreenNodeBuilder<S, A, R>) -> ScreenNode
  ): FlowNodeBuilder<S, A, R> {
    draft.screenBuildActions[nodeKey] = buildAction
    return this
  }

  fun <SR : Any> addFlowNode(
    nodeKey: NodeKey,
    buildAction: (builder: SubFlowBuilder<S, A, R, SR>) -> FlowNode<*, *, SR>
  ): FlowNodeBuilder<S, A, R> {
    draft.flowBuildActions[nodeKey] = buildAction as ((SubFlowBuilder<S, A, R, *>) -> FlowNode<*, *, *>)
    return this
  }

  fun build(initialState: S): Result<FlowNode<S, A, R>, Error> {
    return binding {
      val initial = draft.initial.toResultOr { Error.MissingInitialNode }
      val children = mutableMapOf<NodeKey, Node>()
      draft.screenBuildActions.mapValuesTo(children) { (_, buildAction) ->
        val builder = ScreenNodeBuilder<S, A, R>()
        buildAction(builder)
      }
      draft.flowBuildActions.mapValuesTo(children) { (_, buildAction) ->
        val builder = SubFlowBuilder<S, A, R, Any>()
        buildAction(builder)
      }
      FlowNode(
        initial = initial.bind(),
        children = children,
        eventTransitions = emptyMap(),
        onEntry = null,
        onExit = null,
      )
    }
  }
}
