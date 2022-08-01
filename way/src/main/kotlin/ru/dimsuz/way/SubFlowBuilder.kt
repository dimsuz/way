package ru.dimsuz.way

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import com.github.michaelbull.result.unwrap

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> internal constructor() {
  private var flowNode: FlowNode<*, *, SR>? = null
  private var onResultTransition: ((ResultTransitionEnv<S, A, R, SR>) -> Unit)? = null

  enum class Error {
    MissingFlowNode
  }

  fun of(flow: FlowNode<*, *, SR>): SubFlowBuilder<S, A, R, SR> {
    flowNode = flow
    return this
  }

  fun onFinish(transition: ResultTransitionEnv<S, A, R, SR>.() -> Unit): SubFlowBuilder<S, A, R, SR> {
    onResultTransition = transition
    return this
  }

  fun build(): Result<FlowNode<*, *, SR>, Error> {
    flowNode = (flowNode as FlowNode<Any, *, SR>?)?.let { node ->
      val resultTransition = onResultTransition
      if (resultTransition != null) {
        val builder = node.newBuilder()
        builder.addFinalNode(FlowNode.DEFAULT_FINAL_NODE_KEY) { it.build() }
        builder.on(node.doneEventName, resultTransition as (TransitionEnv<*, *, *>) -> Unit)
        builder
          .build(node.state)
          .unwrap()
      } else node
    }
    return flowNode.toResultOr { Error.MissingFlowNode }
  }
}
