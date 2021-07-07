package ru.dimsuz.way

class NavigationService(
  private val machine: NavigationMachine<*, *, *>,
  private var onCommand: (command: BackStackCommand) -> Unit
) {

  private val history: ArrayDeque<Path> = ArrayDeque()

  fun sendEvent(event: Event) {
    if (history.isEmpty()) {
      error("not started")
    }
    val previousPath = history.last()
    val newPath = machine.transition(history.last(), event)
    val command = produceCommand(history, previousPath, newPath)
    if (previousPath != newPath) {
      history.add(newPath)
    }
    if (command != null) {
      onCommand(command)
    }
  }

  fun start() {
    history.add(machine.initial)
    onCommand(
      BackStackCommand.Replace(
        newBackStack = listOf(BackStackEntry(machine.initial.lastSegment))
      )
    )
  }

  private fun produceCommand(history: List<Path>, previousPath: Path, newPath: Path): BackStackCommand? {
    val existingIndex = history.indexOf(newPath)
    return when {
      previousPath == newPath -> {
        null
      }
      existingIndex != -1 -> {
        BackStackCommand.Pop(count = history.lastIndex - existingIndex)
      }
      else -> {
        BackStackCommand.Push(BackStackEntry(newPath.lastSegment, arguments = null))
      }
    }
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
