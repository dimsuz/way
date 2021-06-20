package ru.dimsuz.way

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> {
  private var flowNode: FlowNode<*, *, SR>? = null

  fun of(flow: FlowNode<*, *, SR>): SubFlowBuilder<S, A, R, SR> {
    flowNode = flow
    return this
  }

  fun onResult(transition: ResultTransitionEnv<S, A, R, SR>.() -> Unit): SubFlowBuilder<S, A, R, SR> {
    return this
  }

  fun build(): FlowNode<*, *, SR> {
    return flowNode ?: error("no flow node specified") // TODO replace with Result
  }
}
