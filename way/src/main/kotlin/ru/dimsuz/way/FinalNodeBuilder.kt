package ru.dimsuz.way

import java.util.UUID

class FinalNodeBuilder<S : Any, A : Any, R : Any> {
  private val eventTransitions = mutableMapOf<Event.Name, (TransitionEnv<*, *, *>) -> Unit>()
  private var onEntry: ((ActionEnv<*, *>) -> Unit)? = null
  private var onExit: ((ActionEnv<*, *>) -> Unit)? = null

  fun onEntry(action: ActionEnv<S, A>.() -> Unit): FinalNodeBuilder<S, A, R> {
    onEntry = action as (ActionEnv<*, *>) -> Unit
    return this
  }

  fun onExit(action: ActionEnv<S, A>.() -> Unit): FinalNodeBuilder<S, A, R> {
    onExit = action as (ActionEnv<*, *>) -> Unit
    return this
  }

  fun on(event: Event.Name, transition: TransitionEnv<S, A, R>.() -> Unit): FinalNodeBuilder<S, A, R> {
    eventTransitions[event] = transition as (TransitionEnv<*, *, *>) -> Unit
    return this
  }

  fun build(): FinalNode {
    return FinalNode(
      eventTransitions = eventTransitions,
      onEntry = onEntry,
      onExit = onExit
    )
  }
}
