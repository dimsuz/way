package ru.dimsuz.way

sealed class Node {
  abstract val eventTransitions: Map<Event, TransitionEnv<*, *, *>>
}
