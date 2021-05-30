package ru.dimsuz.way

open class TransitionEnv<S : Any, A : Any, R : Any> : ActionEnv<S, A>() {
  private var resolvedTargetId: NodeId? = null

  fun navigateTo(id: NodeId) {
    resolvedTargetId = id
  }

  fun navigateTo(path: Path) {
    TODO()
  }

  fun finish(result: R) {
    TODO()
  }

  // TODO make it return sealed class? nodeId/path/result
  fun resolveTarget(): NodeId? {
    return resolvedTargetId
  }
}
