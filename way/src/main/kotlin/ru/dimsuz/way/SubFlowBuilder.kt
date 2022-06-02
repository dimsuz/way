package ru.dimsuz.way

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.toResultOr
import com.github.michaelbull.result.unwrap
import java.util.UUID

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> internal constructor(
  private val extraTransitionsSink: MutableMap<Event.Name, (TransitionEnv<*, *, *>) -> Unit>
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
        val internalDoneEventName = Event.Name("${Event.Name.DONE.value}_${UUID.randomUUID()}")
        extraTransitionsSink[internalDoneEventName] = onResultTransition as (TransitionEnv<*, *, *>) -> Unit
        node.newBuilder()
          .on(Event.Name.DONE) {
            sendEvent(Event(internalDoneEventName, event.payload))
          }
          .build(node.state)
          .unwrap()
      } else node
    }
    return flowNode.toResultOr { Error.MissingFlowNode }
  }
}
