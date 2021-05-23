package ru.dimsuz.way

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.toResultOr

class FlowNodeBuilder<S : Any, A : Any, R : Any> {
  private var draft = FlowNodeDraft<S, A, R>()

  enum class Error {
    MissingInitialNode
  }

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun setInitial(id: NodeId): FlowNodeBuilder<S, A, R> {
    draft.initial = id
    return this
  }

  fun setInitial(transition: TransitionEnv<S, A, R>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun addScreenNode(
    nodeId: NodeId,
    buildAction: (builder: ScreenNodeBuilder<S, A, R>) -> ScreenNode
  ): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun <SR : Any> addFlowNode(
    nodeId: NodeId,
    buildAction: (builder: SubFlowBuilder<S, A, R, SR>) -> FlowNode<*, *, SR>
  ): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun build(initialState: S): Result<FlowNode<S, A, R>, Error> {
    return binding {
      val initial = draft.initial.toResultOr { Error.MissingInitialNode }
      FlowNode(
        initial = initial.bind()
      )
    }
  }
}
