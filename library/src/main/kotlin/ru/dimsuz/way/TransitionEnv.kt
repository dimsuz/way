package ru.dimsuz.way

open class TransitionEnv<S : Any, A : Any, R : Any>(path: Path) : ActionEnv<S, A>(path) {

  private var resolvedTargetPath: Path? = null

  fun navigateTo(key: NodeKey) {
    resolvedTargetPath = if (path.size == 1) Path(key) else path.take(path.size - 1) append key
  }

  fun navigateTo(path: Path) {
    resolvedTargetPath = path
  }

  fun finish(result: R) {
    TODO()
  }

  // TODO make it return sealed class? nodeId/path/result
  fun resolveTarget(): Path? {
    return resolvedTargetPath
  }
}
