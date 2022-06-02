package ru.dimsuz.way

class ScreenNodeBuilder<S : Any, A : Any, R : Any> {
  private val eventTransitions = mutableMapOf<Event.Name, (TransitionEnv<*, *, *>) -> Unit>()
  private var onEntry: ((ActionEnv<*, *>) -> Unit)? = null
  private var onExit: ((ActionEnv<*, *>) -> Unit)? = null

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    onEntry = action as (ActionEnv<*, *>) -> Unit
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    onExit = action as (ActionEnv<*, *>) -> Unit
    return this
  }

  fun on(event: Event.Name, transition: TransitionEnv<S, A, R>.() -> Unit): ScreenNodeBuilder<S, A, R> {
    eventTransitions[event] = transition as (TransitionEnv<*, *, *>) -> Unit
    return this
  }

  fun build(): ScreenNode {
    return ScreenNode(
      eventTransitions = eventTransitions,
      onEntry = onEntry,
      onExit = onExit
    )
  }
}
