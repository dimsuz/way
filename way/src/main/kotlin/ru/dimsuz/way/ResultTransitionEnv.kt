package ru.dimsuz.way

class ResultTransitionEnv<S : Any, R : Any, SR : Any>(
  event: Event,
) : TransitionEnv<S, R>(event) {
  val result: SR get() = event.payload as SR? ?: error("expected finish transition to have a payload")
}
