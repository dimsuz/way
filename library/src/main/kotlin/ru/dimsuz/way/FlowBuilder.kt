package ru.dimsuz.way

interface ActionEnv<S : Any, A : Any> {
  val args: A
  val state: S
  val event: Event?

  fun updateState(transform: (state: S) -> S)
}

@JvmInline
value class Event(val name: String)

@JvmInline
value class NodeId(val id: String)

interface Flow<S : Any, A : Any> {
  fun sendEvent(event: Event)
}

interface Screen

class FlowBuilder<S : Any, A : Any>(val nodeId: NodeId) {
  fun onEntry(action: ActionEnv<S, A>.() -> Unit): FlowBuilder<S, A> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): FlowBuilder<S, A> {
    return this
  }

  fun setInitial(id: NodeId): FlowBuilder<S, A> {
    return this
  }

  fun addScreen(nodeId: NodeId, buildAction: (screenBuilder: ScreenBuilder<S, A>) -> Screen): FlowBuilder<S, A> {
    return this
  }

  fun addFlow(buildAction: (state: S, args: A) -> Flow<*, *>): FlowBuilder<S, A> {
    return this
  }

  fun build(initialState: S): Flow<S, A> {
    return object : Flow<S, A> {
      override fun sendEvent(event: Event) {
      }
    }
  }
}

interface ScreenBuilder<S : Any, A : Any> {

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): ScreenBuilder<S, A> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): ScreenBuilder<S, A> {
    return this
  }
}
