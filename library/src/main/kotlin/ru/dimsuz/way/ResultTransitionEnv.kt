package ru.dimsuz.way

class ResultTransitionEnv<S : Any, A : Any, R : Any, SR : Any>(path: Path) : TransitionEnv<S, A, R>(path) {
  val result: SR
    get() {
      TODO()
    }
}
