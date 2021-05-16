package ru.dimsuz.way

class NavigationMachine<S : Any, A : Any, R : Any>(val root: FlowNode<S, A, R>) {

  val initialNode: NodeId
    get() {
      return NodeId("ttest")
    }

  fun transition(node: NodeId, event: Event): NodeId {
    return NodeId("test")
  }
}
