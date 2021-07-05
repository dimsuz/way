package ru.dimsuz.way

class NavigationService(
  private val machine: NavigationMachine<*, *, *>,
  private var onCommand: (command: BackStackCommand) -> Unit
) {

  private var currentPath = machine.initial

  fun sendEvent(event: Event) {
    val previousPath = currentPath
    currentPath = machine.transition(currentPath, event)
    produceCommand(previousPath, currentPath)?.also(onCommand)
  }

  fun start() {
    onCommand(
      BackStackCommand.Replace(
        newBackStack = listOf(BackStackEntry(machine.initial.lastSegment))
      )
    )
  }

  private fun produceCommand(previousPath: Path, newPath: Path): BackStackCommand? {
    return BackStackCommand.Push(BackStackEntry(newPath.lastSegment, arguments = null))
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
