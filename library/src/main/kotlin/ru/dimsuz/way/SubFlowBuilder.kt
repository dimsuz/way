package ru.dimsuz.way

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import com.github.michaelbull.result.unwrap
import java.util.UUID

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> internal constructor(
  private val extraTransitionsSink: MutableMap<Event, (TransitionEnv<*, *, *>) -> Unit>
) {
  private var flowNode: FlowNode<*, *, SR>? = null
  private var onResultTransition: ((ResultTransitionEnv<S, A, R, SR>) -> Unit)? = null

  enum class Error {
    MissingFlowNode
  }

  fun of(flow: FlowNode<*, *, SR>): SubFlowBuilder<S, A, R, SR> {
    flowNode = flow
    return this
  }

  fun onResult(transition: ResultTransitionEnv<S, A, R, SR>.() -> Unit): SubFlowBuilder<S, A, R, SR> {
    onResultTransition = transition
    return this
  }

  fun build(): Result<FlowNode<*, *, SR>, Error> {
    flowNode = (flowNode as FlowNode<Any, *, SR>?)?.let { node ->
      if (onResultTransition != null) {
        val internalDoneEvent = Event(UUID.randomUUID().toString())
        extraTransitionsSink[internalDoneEvent] = onResultTransition as (TransitionEnv<*, *, *>) -> Unit
        node.newBuilder()
          .on(Event.DONE) { sendEvent(internalDoneEvent) }
          .build(node.state)
          .unwrap()
      } else node
    }
    return flowNode.toResultOr { Error.MissingFlowNode }
  }
}
