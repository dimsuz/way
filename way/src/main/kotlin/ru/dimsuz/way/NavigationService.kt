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
    val transitionResult = machine.transition(backStack.last(), event)
    val newBackStack = recordTransition(backStack, previousPath, transitionResult.path)
    val events = transitionResult.actions?.invoke()
    if (backStack != newBackStack) {
      val oldBackStack = backStack
      backStack = newBackStack
      onCommand(commandBuilder(oldBackStack, newBackStack))
    }
    events?.forEach { sendEvent(it) }
  }

  fun start() {
    backStack = listOf(machine.initial)
    val transitionResult = machine.transitionToInitial()
    val events = transitionResult.actions?.invoke()
    onCommand(commandBuilder(emptyList(), backStack))
    events?.forEach { sendEvent(it) }
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
        backStack.filter { entry ->
          // See NOTE_CLEAR_BACK_STACK_RULES
          var isLeafAlongThePath = false
          var partial: Path? = newPath.parent
          while (partial != null && !isLeafAlongThePath) {
            isLeafAlongThePath = entry.dropLast(1) == partial
            partial = partial.dropLast(1)
          }
          isLeafAlongThePath || entry.parent == newPath.parent
        }.plus(newPath)
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

// NOTE_CLEAR_BACK_STACK_RULES
// TODO document nicely and thoroughly!
// Case1:
//   BackStack: [ m.flowA.S1, m.flowA.S2, m.flowA.flowB.S1 ]
//   Transition: m.flowA.flowB.S1 -> m.flowA.flowB.S2
//   New BackStack: [ m.flowA.S1, m.flowA.S2, m.flowA.flowB.S1, m.flowA.flowB.S2 ]
//   Description: We split state path into components and leave any leaf nodes of all sub-paths,
//                i.e. m.flowA.S1 and m.flowA.S2 must remain in stack although not direct children of m.flowA.flowB.
//                All others are cleared
// Case2:
//   BackStack: [ m.flowA.S1, m.flowA.S2, m.flowA.flowB.S1 ]
//   Transition: m.flowA.flowB.S1 -> m.flowA.flowC.S1
//   New BackStack: [ m.flowA.S1, m.flowA.S2, m.flowA.flowC.S1 ]
//   Description: m.flowA.flowB.S1 must go away due to the logic described in Case1
