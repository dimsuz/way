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

  fun transition(path: Path, event: Event): Path {
    val node = root.findChild(path)
      ?: error("no node at $path found")
    val transitionSpec = node.eventTransitions[event]
    return if (transitionSpec != null) {
      val transition = TransitionEnv<S, A, R>(path).apply(transitionSpec)
      val targetPath = transition.resolveTarget()
        ?: error("expected transition target for path = $path")
      root.fullyResolvePath(targetPath)
    } else {
      path
    }
  }
}

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
