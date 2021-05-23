package ru.dimsuz.way

interface TransitionEnv<S : Any, A : Any, R : Any> : ActionEnv<S, A> {
  fun navigateTo(id: NodeId)
  fun navigateTo(path: Path)
  fun finish(result: R)
}
