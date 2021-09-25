package ru.dimsuz.way

class ResultTransitionEnv<S : Any, A : Any, R : Any, SR : Any>(
  path: Path,
  val result: SR
) : TransitionEnv<S, A, R>(path)
