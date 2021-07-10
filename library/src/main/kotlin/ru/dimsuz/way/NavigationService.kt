package ru.dimsuz.way

class NavigationService<T : Any>(
  private val machine: NavigationMachine<*, *, *>,
  private val commandBuilder: CommandBuilder<T>,
  private var onCommand: (command: T) -> Unit
) {

  private var backStack: BackStack = emptyList()

  fun sendEvent(event: Event) {
    if (backStack.isEmpty()) {
      error("not started")
    }
    val previousPath = backStack.last()
    val newPath = machine.transition(backStack.last(), event)
    val newBackStack = recordTransition(backStack, previousPath, newPath)
    if (backStack != newBackStack) {
      val oldBackStack = backStack
      backStack = newBackStack
      onCommand(commandBuilder(oldBackStack, newBackStack))
    }
  }

  fun start() {
    backStack = listOf(machine.initial)
    onCommand(commandBuilder(emptyList(), backStack))
  }

  private fun recordTransition(backStack: BackStack, previousPath: Path, newPath: Path): BackStack {
    val existingIndex = backStack.indexOf(newPath)
    return when {
      previousPath == newPath -> {
        backStack
      }
      existingIndex != -1 -> {
        backStack.dropLast(backStack.lastIndex - existingIndex)
      }
      else -> {
        backStack.plus(newPath)
      }
    }
  }
}

typealias CommandBuilder<T> = (oldBackStack: BackStack, newBackStack: BackStack) -> T
typealias BackStack = List<Path>

sealed class BackStackCommand {
  data class Push(val path: BackStackEntry) : BackStackCommand()
  data class Pop(val count: Int) : BackStackCommand()
  data class Replace(val newBackStack: List<BackStackEntry>) : BackStackCommand()
}

data class BackStackEntry(
  val key: NodeKey,
  val arguments: Any? = null,
)
