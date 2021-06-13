package ru.dimsuz.way

class ScreenNodeBuilder<S : Any, A : Any, R : Any> {
  private val eventTransitions = mutableMapOf<Event, (TransitionEnv<*, *, *>) -> Unit>()

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    return this
  }

  fun on(event: Event, transition: TransitionEnv<S, A, R>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    eventTransitions[event] = transition as (TransitionEnv<*, *, *>) -> Unit
    return this
  }

  fun build(): ScreenNode {
    return ScreenNode(
      eventTransitions = eventTransitions
    )
  }
}
