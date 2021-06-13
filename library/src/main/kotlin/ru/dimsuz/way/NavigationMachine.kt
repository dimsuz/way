package ru.dimsuz.way

class NavigationMachine<S : Any, A : Any, R : Any>(val root: FlowNode<S, A, R>) {
  val initialNodeId: NodeId
    get() {
      return root.initial
    }

  fun transition(nodeId: NodeId, event: Event): NodeId {
    val node = root.findChild(nodeId)
      ?: error("no node with id = $nodeId found")
    val transitionSpec = node.eventTransitions[event]
    if (transitionSpec != null) {
      val transition = TransitionEnv<S, A, R>().apply(transitionSpec)
      return transition.resolveTarget()
        ?: error("expected transition target for node id = $nodeId")
    }
    return nodeId
  }
}

private fun FlowNode<*, *, *>.findChild(id: NodeId): Node? {
  children.forEach { (childId, child) ->
    when (child) {
      is FlowNode<*, *, *> -> {
        return child.findChild(id)
      }
      is ScreenNode -> {
        if (childId == id) {
          return child
        }
      }
    }
  }
  return null
}
