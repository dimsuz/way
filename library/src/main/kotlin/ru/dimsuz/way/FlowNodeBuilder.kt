package ru.dimsuz.way

interface ActionEnv<S : Any, A : Any> {
  val args: A
  val state: S
  val event: Event?
  val path: Path

  fun updateState(transform: (state: S) -> S)
}

interface TransitionEnv<S : Any, A : Any, R : Any> : ActionEnv<S, A> {
  fun navigateTo(id: NodeId)
  fun navigateTo(path: Path)
  fun finish(result: R)
}

interface ResultTransitionEnv<S : Any, A : Any, R : Any, SR : Any> : TransitionEnv<S, A, R> {
  val result: SR
}

@JvmInline
value class Event(val name: String) {
  companion object {
    val BACK = Event("BACK")
  }
}

@JvmInline
value class NodeId(val id: String)

@JvmInline
value class Path(val segments: List<NodeId>)

interface FlowNode<S : Any, A : Any, R : Any>

interface ScreenNode

class FlowNodeBuilder<S : Any, A : Any, R : Any> {
  fun onEntry(action: ActionEnv<S, A>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun setInitial(id: NodeId): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun setInitial(transition: TransitionEnv<S, A, R>.() -> Unit) : FlowNodeBuilder<S, A, R> {
    return this
  }

  fun addScreenNode(nodeId: NodeId, buildAction: (builder: ScreenNodeBuilder<S, A, R>) -> ScreenNode): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun <SR : Any> addFlowNode(nodeId: NodeId, buildAction: (builder: SubFlowBuilder<S, A, R, SR>) -> FlowNode<*, *, SR>): FlowNodeBuilder<S, A, R> {
    return this
  }

  fun build(initialState: S): FlowNode<S, A, R> {
    return object : FlowNode<S, A, R> {
    }
  }
}

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

interface ScreenNodeBuilder<S : Any, A : Any, R : Any> {

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    return this
  }

  fun on(event: Event, transition: TransitionEnv<S, A, R>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    return this
  }

  fun build(): ScreenNode {
    return object : ScreenNode {

    }
  }
}
