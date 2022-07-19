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
    val events = mutableListOf<Event>()
    transitionResult.actions?.forEach { action ->
      val result = action()
      val node = machine.root.findChild(result.parentFlowNodePath)
      result.updatedState?.let { (node as FlowNode<Any, *, *>).setState(it) }
      events.addAll(result.events)
    }
    if (backStack != newBackStack) {
      val oldBackStack = backStack
      backStack = newBackStack
      // final node states are not supposed to be rendered, they are transitory, so do not pass them to command handler
      // but it is still required to save full backstack internally so that the next transition will receive a
      // correct previous state (even when it's a 'done' final state)
      val filteredOldBackStack = oldBackStack.removeInternalNodeEntries()
      val filteredNewBackStack = newBackStack.removeInternalNodeEntries()
      if (filteredOldBackStack != filteredNewBackStack) {
        onCommand(
          commandBuilder(
            filteredOldBackStack,
            filteredNewBackStack,
            machine.root.findAncestorFlowNodeState(transitionResult.path)
          )
        )
      }
    }
    events.forEach { sendEvent(it) }
  }

  private fun BackStack.removeInternalNodeEntries(): BackStack {
    return if (this.lastOrNull()?.lastSegment == FlowNode.DEFAULT_FINAL_NODE_KEY) {
      this.dropLast(1)
    } else this
  }

  fun start() {
    backStack = listOf(machine.initial)
    val transitionResult = machine.transitionToInitial()
    val events = mutableListOf<Event>()
    transitionResult.actions?.forEach { action ->
      val result = action()
      val node = machine.root.findChild(result.parentFlowNodePath)
      result.updatedState?.let { (node as FlowNode<Any, *, *>).setState(it) }
      events.addAll(result.events)
    }
    onCommand(commandBuilder(
      emptyList(),
      backStack,
      machine.root.findAncestorFlowNodeState(transitionResult.path)
    ))
    events.forEach { sendEvent(it) }
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

typealias CommandBuilder<T> = (oldBackStack: BackStack, newBackStack: BackStack, newState: Any) -> T
typealias BackStack = List<Path>

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
