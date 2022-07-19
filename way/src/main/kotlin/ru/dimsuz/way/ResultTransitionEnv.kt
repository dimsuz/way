package ru.dimsuz.way

class ResultTransitionEnv<S : Any, A : Any, R : Any, SR : Any>(
  event: Event,
) : TransitionEnv<S, A, R>(event) {
  val result: SR get() = event.payload as SR? ?: error("expected finish transition to have a payload")
}
