package ru.dimsuz.way

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> {
  fun of(flow: FlowNode<*, *, SR>): SubFlowBuilder<S, A, R, SR> {
    return this
  }

  fun onResult(transition: ResultTransitionEnv<S, A, R, SR>.() -> Unit): SubFlowBuilder<S, A, R, SR> {
    return this
  }

  fun build(): FlowNode<*, *, SR> {
    TODO()
  }
}
