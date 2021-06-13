package ru.dimsuz.way

open class TransitionEnv<S : Any, A : Any, R : Any> : ActionEnv<S, A>() {
  private var resolvedTargetKey: NodeKey? = null

  fun navigateTo(key: NodeKey) {
    resolvedTargetKey = key
  }

  fun navigateTo(path: Path) {
    TODO()
  }

  fun finish(result: R) {
    TODO()
  }

  // TODO make it return sealed class? nodeId/path/result
  fun resolveTarget(): NodeKey? {
    return resolvedTargetKey
  }
}
