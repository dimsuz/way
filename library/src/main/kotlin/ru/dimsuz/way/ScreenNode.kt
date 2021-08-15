package ru.dimsuz.way

data class ScreenNode internal constructor(
  override val eventTransitions: Map<Event, (TransitionEnv<*, *, *>) -> Unit>,
  override val onEntry: ((ActionEnv<*, *>) -> Unit)?,
  override val onExit: ((ActionEnv<*, *>) -> Unit)?
) : Node
