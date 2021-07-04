package ru.dimsuz.way

class NavigationService(
  val machine: NavigationMachine<*, *, *>,
  var onCommand: (command: BackStackCommand) -> Unit
) {
  fun sendEvent(event: Event) {
  }
}

sealed class BackStackCommand {
  data class Push(val entry: BackStackEntry) : BackStackCommand()
  data class Pop(val count: Int) : BackStackCommand()
  data class Replace(val newBackStack: List<BackStackEntry>) : BackStackCommand()
}

data class BackStackEntry(
  val key: NodeKey,
  val arguments: Any? = null,
)
