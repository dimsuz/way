package ru.dimsuz.way

sealed interface Node {
  val eventTransitions: Map<Event.Name, (TransitionEnv<*, *, *>) -> Unit>
  val onEntry: ((ActionEnv<*, *>) -> Unit)?
  val onExit: ((ActionEnv<*, *>) -> Unit)?
}
