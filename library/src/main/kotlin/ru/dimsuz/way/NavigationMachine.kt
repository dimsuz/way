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
    val entrySet = root.findChildrenAlongPath(initial)
    val transitionEnv = TransitionEnv<S, A, R>(initial)
    val list = mutableListOf<(ActionEnv<*, *>) -> Unit>()
    root.onEntry?.also { list.add(it) }
    entrySet.forEach { node ->
      node.onEntry?.also { list.add(it) }
    }
    return TransitionResult(
      path = initial,
      actions = list.composeIn(transitionEnv)
    )
  }

  private fun List<(ActionEnv<*, *>) -> Unit>.composeIn(env: ActionEnv<*, *>): () -> List<Event> {
    return {
      this.forEach { action -> action(env) }
      env.queuedEvents.orEmpty()
    }
  }

  @Suppress("MoveLambdaOutsideParentheses")
  fun transition(path: Path, event: Event): TransitionResult {
    val resolvedTransition = findAndResolveTransitionTarget(root, event, path)
    return if (resolvedTransition?.targetPath != null && resolvedTransition.finishResult == null) {
      val resolvedTargetPath = root.fullyResolvePath(resolvedTransition.targetPath)

      val actionEnv = ActionEnv<S, A>(path)
      val exitSet = findExitNodes(root, path, resolvedTargetPath)
      val actions = mutableListOf<(ActionEnv<*, *>) -> Unit>()
      exitSet.forEach { n ->
        if (n.onExit != null) n.onExit?.let { actions.add(it) }
      }
      val entrySet = findEntryNodes(root, path, resolvedTargetPath)
      entrySet.forEach { n ->
        if (n.onEntry != null) n.onEntry?.let { actions.add(it) }
      }

      TransitionResult(resolvedTargetPath, actions.takeIf { it.isNotEmpty() }?.composeIn(actionEnv))
    } else if (resolvedTransition?.targetPath == null && resolvedTransition?.finishResult != null) {
      TransitionResult(path, actions = { listOf(Event.DONE) })
    } else if (resolvedTransition?.targetPath != null && resolvedTransition.finishResult != null) {
      error("only one of navigateTo() or finish() can be called")
    } else if (resolvedTransition != null && resolvedTransition.targetPath == null &&
      resolvedTransition.finishResult == null && resolvedTransition.queuedEvents.isNotEmpty()
    ) {
      TransitionResult(path, actions = { resolvedTransition.queuedEvents })
    } else {
      println("no transition for event $event on node $path, ignoring")
      TransitionResult(path, actions = null)
    }
  }

  private fun findAndResolveTransitionTarget(root: FlowNode<*, *, *>, event: Event, path: Path): ResolvedTransition? {
    val nodes = root.findChildrenAlongPath(path).takeIf { it.isNotEmpty() }
      ?: error("no nodes at $path found")
    var transition: ((TransitionEnv<*, *, *>) -> Unit)? = null
    var transitionNodePath: Path? = null
    for (i in nodes.lastIndex downTo 0) {
      val transitionSpec = nodes[i].eventTransitions[event]
      if (transitionSpec != null) {
        transition = transitionSpec
        transitionNodePath = path.take(i + 1)
        break
      }
    }
    if (transitionNodePath == null) {
      // no node in path contains matching transition, maybe root has it?
      val transitionSpec = root.eventTransitions[event]
      if (transitionSpec != null) {
        transition = transitionSpec
        transitionNodePath = Path(NODE_KEY_ROOT)
      }
    }
    return (transition ?: root.eventTransitions[event])
      ?.let { spec ->
        val transitionEnv = ResultTransitionEnv<S, A, R, Any>(path, "finishResultT")
        spec.invoke(transitionEnv)
        ResolvedTransition(
          targetPath = transitionEnv.destination?.resolve(transitionNodePath),
          finishResult = transitionEnv.finishResult,
          queuedEvents = transitionEnv.queuedEvents.orEmpty()
        )
      }
  }

  private fun TransitionEnv.Destination.resolve(transitionNodePath: Path?): Path {
    return when (this) {
      is TransitionEnv.Destination.Path -> this.path
      is TransitionEnv.Destination.RelativeNode -> {
        val resolveContext = transitionNodePath
          ?: error("unexpected null path for resolve. destination = $this")
        resolveContext.dropLast(1)?.let { it append this.key } ?: Path(this.key)
      }
    }
  }
}

private data class ResolvedTransition(
  val targetPath: Path?,
  val finishResult: Any?,
  val queuedEvents: List<Event>
)

private fun List<() -> Unit>.join(): () -> Unit {
  return { for (a in this) a() }
}

data class TransitionResult(
  val path: Path,
  val actions: (() -> List<Event>)?,
)

private fun findEntryNodes(root: FlowNode<*, *, *>, path: Path, newPath: Path): List<Node> {
  val commonPrefixSize = newPath.findCommonPrefix(path)?.size ?: 0
  return root.findChildrenAlongPath(newPath).drop(commonPrefixSize)
}

private fun findExitNodes(root: FlowNode<*, *, *>, path: Path, newPath: Path): List<Node> {
  val commonPrefixSize = newPath.findCommonPrefix(path)?.size ?: 0
  return root.findChildrenAlongPath(path).drop(commonPrefixSize)
}

/**
 * "flowA.flowB.screenA".isChildOf("flowA") // true
 * "flowA.flowB.screenA".isChildOf("flowA.flowB") // true
 * "flowA.flowB.screenA".isChildOf("flowC") // false
 * "flowA.flowB.screenA".isChildOf("flowA.flowC") // false
 */
private fun Path.isChildOf(other: Path): Boolean {
  return this.findCommonPrefix(other) == other
}

/**
 * "flowA.flowB.screenA".isChildOf("flowA") // [ flowA ]
 * "flowA.flowB.screenA".isChildOf("flowA.flowB") // [ flowA, flowB ]
 * "flowA.flowB.screenA".isChildOf("flowC") // [ ]
 * "flowA.flowB.screenA".isChildOf("flowA.flowC") // [ flowA ]
 */
private fun Path.findCommonPrefix(other: Path): Path? {
  var p1: Path? = this
  var p2: Path? = other
  val result = ArrayList<NodeKey>(minOf(this.size, other.size))
  while (p1 != null && p2 != null) {
    if (p1.firstSegment == p2.firstSegment) {
      result.add(p1.firstSegment)
      p1 = p1.tail
      p2 = p2.tail
    } else {
      break
    }
  }
  return if (result.isNotEmpty()) Path.fromNonEmptyListOf(result) else null
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

private fun FlowNode<*, *, *>.findChildrenAlongPath(path: Path): List<Node> {
  var p: Path? = path
  var n: FlowNode<*, *, *>? = this

  val nodes = mutableListOf<Node>()

  while (p != null && n != null) {
    val key = n.children.keys.find { it == p!!.firstSegment } ?: return emptyList()
    val node = n.children[key] ?: return emptyList()
    nodes.add(node)
    p = p.tail
    n = when (node) {
      is FlowNode<*, *, *> -> node
      is ScreenNode -> null
    }
  }
  return nodes
}

private val NODE_KEY_ROOT = NodeKey("<root>")
