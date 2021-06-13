package ru.dimsuz.way

sealed interface Node {
  val eventTransitions: Map<Event, (TransitionEnv<*, *, *>) -> Unit>
}
