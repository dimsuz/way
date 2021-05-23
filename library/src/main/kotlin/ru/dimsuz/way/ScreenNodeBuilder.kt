package ru.dimsuz.way

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
