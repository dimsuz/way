package ru.dimsuz.way

data class ScreenNode(
  override val eventTransitions: Map<Event, TransitionEnv<*, *, *>>
) : Node()
