package ru.dimsuz.way

data class ScreenNode(
  val eventTransitions: Map<Event, TransitionEnv<*, *, *>>
)
