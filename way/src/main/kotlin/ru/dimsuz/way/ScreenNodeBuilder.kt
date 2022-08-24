package ru.dimsuz.way

class ScreenNodeBuilder<S : Any, R : Any> {
  private val eventTransitions = mutableMapOf<Event.Name, (TransitionEnv<*, *>) -> Unit>()
  private var onEntry: ((ActionEnv<*>) -> Unit)? = null
  private var onExit: ((ActionEnv<*>) -> Unit)? = null

  fun onEntry(action: ActionEnv<S>.() -> Unit): ScreenNodeBuilder<S, R> {
    onEntry = action as (ActionEnv<*>) -> Unit
    return this
  }

  fun onExit(action: ActionEnv<S>.() -> Unit): ScreenNodeBuilder<S, R> {
    onExit = action as (ActionEnv<*>) -> Unit
    return this
  }

  fun on(event: Event.Name, transition: TransitionEnv<S, R>.() -> Unit): ScreenNodeBuilder<S, R> {
    eventTransitions[event] = transition as (TransitionEnv<*, *>) -> Unit
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
