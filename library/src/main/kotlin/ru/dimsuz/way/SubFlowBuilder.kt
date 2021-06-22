package ru.dimsuz.way

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> {
  private var flowNode: FlowNode<*, *, SR>? = null

  enum class Error {
    MissingFlowNode
  }

  fun of(flow: FlowNode<*, *, SR>): SubFlowBuilder<S, A, R, SR> {
    flowNode = flow
    return this
  }

  fun onResult(transition: ResultTransitionEnv<S, A, R, SR>.() -> Unit): SubFlowBuilder<S, A, R, SR> {
    return this
  }

  fun build(): Result<FlowNode<*, *, SR>, Error> {
    return flowNode.toResultOr { Error.MissingFlowNode }
  }
}
