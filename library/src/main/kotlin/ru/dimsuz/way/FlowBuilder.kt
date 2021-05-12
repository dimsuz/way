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

interface Flow<S : Any, A : Any, R : Any> {
  fun sendEvent(event: Event)
}

interface Screen

class FlowBuilder<S : Any, A : Any, R : Any>(val nodeId: NodeId) {
  fun onEntry(action: ActionEnv<S, A>.() -> Unit): FlowBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): FlowBuilder<S, A, R> {
    return this
  }

  fun setInitial(id: NodeId): FlowBuilder<S, A, R> {
    return this
  }

  fun addScreen(nodeId: NodeId, buildAction: (screenBuilder: ScreenBuilder<S, A, R>) -> Screen): FlowBuilder<S, A, R> {
    return this
  }

//  fun addFlow(buildAction: (state: S, args: A) -> Flow<*, *, *>): FlowBuilder<S, A, R> {
//    return this
//  }

  fun <SR : Any> addFlow(buildAction: (subFlowBuilder: SubFlowBuilder<S, A, R, SR>) -> Flow<*, *, SR>): FlowBuilder<S, A, R> {
    return this
  }

  fun build(initialState: S): Flow<S, A, R> {
    return object : Flow<S, A, R> {
      override fun sendEvent(event: Event) {
      }
    }
  }
}

class SubFlowBuilder<S : Any, A : Any, R : Any, SR : Any> {
  fun of(flow: Flow<*, *, SR>): SubFlowBuilder<S, A, R, SR> {
    return this
  }

  fun onResult(result: SR, transition: TransitionEnv<S, A, R>.() -> Unit): SubFlowBuilder<S, A, R, SR> {
    return this
  }

  fun build(): Flow<*, *, SR> {
    TODO()
  }
}

interface ScreenBuilder<S : Any, A : Any, R : Any> {

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): ScreenBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): ScreenBuilder<S, A, R> {
    return this
  }

  fun on(event: Event, transition: TransitionEnv<S, A, R>.() -> Unit): ScreenBuilder<S, A, R> {
    return this
  }

  fun build(): Screen {
    return object : Screen {

    }
  }
}
