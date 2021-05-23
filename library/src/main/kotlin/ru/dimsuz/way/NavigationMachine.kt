package ru.dimsuz.way

class NavigationMachine<S : Any, A : Any, R : Any>(val root: FlowNode<S, A, R>) {

  val initialNode: NodeId
    get() {
      return root.initial
    }

  fun transition(node: NodeId, event: Event): NodeId {
    return NodeId("test")
  }
}
