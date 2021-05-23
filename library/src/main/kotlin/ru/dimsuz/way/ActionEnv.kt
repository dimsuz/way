package ru.dimsuz.way

interface ActionEnv<S : Any, A : Any> {
  val args: A
  val state: S
  val event: Event?
  val path: Path

  fun updateState(transform: (state: S) -> S)
}
