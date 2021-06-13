package ru.dimsuz.way

class NavigationMachine<S : Any, A : Any, R : Any>(val root: FlowNode<S, A, R>) {
  val initialNodeKey: NodeKey
    get() {
      return root.initial
    }

  fun transition(nodeKey: NodeKey, event: Event): NodeKey {
    val node = root.findChild(nodeKey)
      ?: error("no node with id = $nodeKey found")
    val transitionSpec = node.eventTransitions[event]
    if (transitionSpec != null) {
      val transition = TransitionEnv<S, A, R>().apply(transitionSpec)
      return transition.resolveTarget()
        ?: error("expected transition target for node id = $nodeKey")
    }
    return nodeKey
  }
}

private fun FlowNode<*, *, *>.findChild(key: NodeKey): Node? {
  children.forEach { (childKey, child) ->
    when (child) {
      is FlowNode<*, *, *> -> {
        return child.findChild(key)
      }
      is ScreenNode -> {
        if (childKey == key) {
          return child
        }
      }
    }
  }
  return null
}
