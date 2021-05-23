package ru.dimsuz.way

interface ResultTransitionEnv<S : Any, A : Any, R : Any, SR : Any> : TransitionEnv<S, A, R> {
  val result: SR
}
