package ru.dimsuz.way

class NavigationMachine<S : Any, A : Any, R : Any>(val root: FlowNode<S, A, R>) {
  val initial: Path
    get() {
      return Path(root.initial)
    }

  fun transition(path: Path, event: Event): Path {
    val node = root.findChild(path)
      ?: error("no node at $path found")
    val transitionSpec = node.eventTransitions[event]
    if (transitionSpec != null) {
      val transition = TransitionEnv<S, A, R>(path).apply(transitionSpec)
      return transition.resolveTarget()
        ?: error("expected transition target for path = $path")
    }
    return path
  }
}

private fun FlowNode<*, *, *>.findChild(path: Path): Node? {
  val first = path.firstSegment

  val key = children.keys.find { it == first } ?: return null
  val node = children[key] ?: return null
  return when (node) {
    is FlowNode<*, *, *> -> path.tail?.let { node.findChild(it) }
    is ScreenNode -> if (path.tail == null) node else null
  }
}
