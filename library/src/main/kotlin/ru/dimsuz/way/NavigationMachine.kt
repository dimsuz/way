package ru.dimsuz.way

class NavigationMachine<S : Any, A : Any, R : Any>(val root: FlowNode<S, A, R>) {

  val initial: Path
    get() {
      return generateSequence(seed = Path(root.initial) to false) { (path, _) ->
        when (val node = root.findChild(path)) {
          is FlowNode<*, *, *> -> {
            (path append node.initial) to false
          }
          is ScreenNode -> path to true
          null -> error("no initial node for path $path")
        }
      }.takeWhile { (_, isAtomic) -> !isAtomic }.last().first
    }

  fun transitionToInitial(): TransitionResult {
    val node = root.findChild(initial)
      ?: error("no node at $initial found")
    val transitionEnv = TransitionEnv<S, A, R>(initial)
    return TransitionResult(initial, actions = { node.onEntry?.invoke(transitionEnv) })
  }

  @Suppress("MoveLambdaOutsideParentheses")
  fun transition(path: Path, event: Event): TransitionResult {
    val node = root.findChild(path)
      ?: error("no node at $path found")
    val transitionSpec = node.eventTransitions[event]
    return if (transitionSpec != null) {
      val transitionEnv = TransitionEnv<S, A, R>(path)
      val actions = mutableListOf<() -> Unit>()
      val transition = transitionEnv.apply(transitionSpec)
      val targetPath = transition.resolveTarget()
        ?: error("expected transition target for path = $path")
      val resolvedTargetPath = root.fullyResolvePath(targetPath)
      val targetNode = root.findChild(resolvedTargetPath)
        ?: error("no node at $resolvedTargetPath found")

      if (node.onExit != null) {
        actions.add({ node.onExit?.invoke(transitionEnv) })
      }
      if (targetNode.onEntry != null) {
        actions.add({ targetNode.onEntry?.invoke(transitionEnv) })
      }

      TransitionResult(resolvedTargetPath, actions.takeIf { it.isNotEmpty() }?.join())
    } else {
      TransitionResult(path, actions = null)
    }
  }
}

private fun List<() -> Unit>.join(): () -> Unit {
  return { for (a in this) a() }
}

data class TransitionResult(
  val path: Path,
  val actions: (() -> Unit)?,
)

// TODO replace this with fold + traverse and/or give it a clearer name
private fun FlowNode<*, *, *>.fullyResolvePath(targetPath: Path): Path {
  return when (val targetNode = this.findChild(targetPath)) {
    is FlowNode<*, *, *> -> {
      fullyResolvePath(targetPath append targetNode.initial)
    }
    is ScreenNode -> targetPath
    null -> error("machine has no node \"$targetPath\"")
  }
}

private fun FlowNode<*, *, *>.findChild(path: Path): Node? {
  val first = path.firstSegment

  val key = children.keys.find { it == first } ?: return null
  val node = children[key] ?: return null
  val tail = path.tail
  return if (tail != null) {
    when (node) {
      is FlowNode<*, *, *> -> node.findChild(tail)
      is ScreenNode -> null
    }
  } else {
    node
  }
}
