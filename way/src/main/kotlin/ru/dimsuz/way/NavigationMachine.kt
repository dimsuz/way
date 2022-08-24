package ru.dimsuz.way

class NavigationMachine<S : Any, R : Any>(val root: FlowNode<S, R>) {

  val initial: Path
    get() {
      return generateSequence(seed = Path(root.initial) to false) { (path, _) ->
        when (val node = root.findChild(path)) {
          is FlowNode<*, *> -> {
            (path append node.initial) to false
          }
          is ScreenNode -> path to true
          is FinalNode -> path to true
          null -> error("no initial node for path $path")
        }
      }.takeWhile { (_, isAtomic) -> !isAtomic }.last().first
    }

  fun transitionToInitial(): TransitionResult {
    val entrySet = findEntryNodes(Path(NODE_KEY_ROOT), initial)
    val actions = mutableListOf<() -> ActionResult>()
    val actionEnv = ActionEnv(Event(Event.Name.INIT), readState = { root.state })
    root.onEntry?.also { actions.add(it.bindTo(actionEnv, Path(NODE_KEY_ROOT))) }
    entrySet.forEach { nodePath ->
      val n = root.findChild(nodePath) ?: error("failed to find node at $nodePath")
      val ancestorFlowPath = root.findAncestorFlowNodePath(nodePath)
      val env = ActionEnv(Event(Event.Name.INIT), readState = { root.findAncestorFlowNodeState(ancestorFlowPath) } )
      if (n.onEntry != null) n.onEntry?.let { actions.add(it.bindTo(env, ancestorFlowPath)) }
    }
    return TransitionResult(
      path = initial,
      actions = actions
    )
  }

  fun transition(path: Path, event: Event): TransitionResult {
    val transitionsDebugLog = if (DEBUG_LOG_TRANSITIONS) StringBuilder() else null
    if (DEBUG_LOG_TRANSITIONS) {
      transitionsDebugLog?.append("transition [$path] x ${event.name.value} → ")
    }
    val resolvedTransition = findAndResolveTransitionTarget(root, event, path)
    val result = if (resolvedTransition?.targetPath != null) {
      val resolvedTargetPath = root.fullyResolvePath(resolvedTransition.targetPath)

      val exitSet = findExitNodes(path, resolvedTargetPath)
      val actions = mutableListOf<() -> ActionResult>()
      exitSet.forEach { nodePath ->
        val n = root.findChild(nodePath) ?: error("failed to find node at $nodePath")
        val env = ActionEnv(event, readState = { root.findAncestorFlowNodeState(nodePath) })
        if (n.onExit != null) n.onExit?.let {
          val ancestorFlowPath = root.findAncestorFlowNodePath(nodePath)
          actions.add(it.bindTo(env, ancestorFlowPath))
        }
      }
      val entrySet = findEntryNodes(path, resolvedTargetPath)
      entrySet.forEach { nodePath ->
        val n = root.findChild(nodePath) ?: error("failed to find node at $nodePath")
        val env = ActionEnv(event, readState = { root.findAncestorFlowNodeState(nodePath) })
        if (n.onEntry != null) n.onEntry?.let {
          val ancestorFlowPath = root.findAncestorFlowNodePath(nodePath)
          actions.add(it.bindTo(env, ancestorFlowPath))
        }
      }
      if (resolvedTransition.queuedEvents.isNotEmpty()) {
        val ancestorFlowPath = root.findAncestorFlowNodePath(path)
        @Suppress("MoveLambdaOutsideParentheses")
        actions.add({ ActionResult(resolvedTransition.queuedEvents, ancestorFlowPath, null) })
      }

      TransitionResult(resolvedTargetPath, actions.takeIf { it.isNotEmpty() })
    } else if (resolvedTransition?.queuedEvents?.isNotEmpty() == true) {
      val ancestorFlowPath = root.findAncestorFlowNodePath(path)
      @Suppress("MoveLambdaOutsideParentheses")
      TransitionResult(path, actions = listOf({
        ActionResult(
          resolvedTransition.queuedEvents,
          parentFlowNodePath = ancestorFlowPath,
          updatedState = null
        )
      }))
    } else {
      println("no transition for event $event on node $path or its ancestors, ignoring")
      TransitionResult(path, actions = null)
    }

    return result.also {
      if (DEBUG_LOG_TRANSITIONS) {
        if (it.actions != null) {
          transitionsDebugLog?.appendLine("${it.path} <with actions>")
        } else {
          transitionsDebugLog?.appendLine("${it.path}")
        }
        transitionsDebugLog?.let { log -> println(log.toString()) }
      }
    }
  }

  private fun findAndResolveTransitionTarget(root: FlowNode<*, *>, event: Event, path: Path): ResolvedTransition? {
    val nodes = root.findChildrenAlongPath(path).takeIf { it.isNotEmpty() }
      ?: error("no nodes at $path found")
    var transition: ((TransitionEnv<*, *>) -> Unit)? = null
    var transitionNode: Node? = null
    var transitionNodePath: Path? = null

    for (i in nodes.lastIndex downTo 0) {
      val transitionSpec = nodes[i].eventTransitions[event.name]
      if (transitionSpec != null) {
        transition = transitionSpec
        transitionNodePath = path.take(i + 1)
        transitionNode = nodes[i]
        break
      }
    }
    if (transitionNodePath == null) {
      // no node in path contains matching transition, maybe root has it?
      val transitionSpec = root.eventTransitions[event.name]
      if (transitionSpec != null) {
        transition = transitionSpec
        transitionNodePath = Path(NODE_KEY_ROOT)
        transitionNode = root
      }
    }

    return (transition ?: root.eventTransitions[event.name])
      ?.let { spec ->
        // See NOTE_FINISH_TRANSITIONS
        val transitionEnv = if (event.name.isDone()) {
          ResultTransitionEnv<Any, R, Any>(event)
        } else {
          TransitionEnv(event)
        }
        spec.invoke(transitionEnv)
        // NOTE_FINISH_TRANSITIONS
        // On call to finish() we do the following things:
        // 1. Switch to the final node of the current flow (which is always present, ensured by the FlowNodeBuilder)
        // 2. Raise an "internal" DONE-event, specific to that flow, which will trigger a special "done"-transition,
        //    defined on the Flow node (also ensured by the FlowNodeBuilder)
        val destination = if (transitionEnv.finishResult != null) {
          TransitionEnv.Destination.RelativeNode(FlowNode.DEFAULT_FINAL_NODE_KEY)
        } else transitionEnv.destination

        if (event.name.isDone()) {
          // when resolving destinations for internal "done"-transitions, it needs to be done in the context
          // of the parent flow of the done-flow
          transitionNodePath = transitionNodePath?.let { root.findAncestorFlowNodePath(it, ignoreCurrent = true) }
          transitionNode = transitionNodePath?.let { root.findChild(it) ?: root }
        }

        val targetPath = destination?.resolve(transitionNodePath, transitionNode)

        val doneEvent = if (transitionEnv.finishResult != null && targetPath != null) {
          val ancestorFlowNode = root.findAncestorFlowNode(targetPath)
          Event(ancestorFlowNode.doneEventName, transitionEnv.finishResult)
        } else null

        ResolvedTransition(
          targetPath = targetPath,
          queuedEvents = transitionEnv.queuedEvents.orEmpty().let { if (doneEvent != null) it + doneEvent else it }
        )
      }
  }

  private fun TransitionEnv.Destination.resolve(transitionNodePath: Path?, transitionNode: Node?): Path {
    return when (this) {
      is TransitionEnv.Destination.Path -> this.path
      is TransitionEnv.Destination.RelativeNode -> {
        // When resolving relative destinations is required to differentiate between flows and paths:
        //  - if resolving destination "screenB" against screen node "flowA.screenA", result must be
        //    "flowA.screenB", i.e. dropLast(1) must be used
        //  - if resolving destination "screenB" against flow node "flowA", dropLast is not needed
        requireNotNull(transitionNodePath) { "unexpected null path for resolve. destination = $this" }
        requireNotNull(transitionNode) { "unexpected null node for resolve. destination = $this" }
        if (transitionNode is FlowNode<*, *>) {
          if (transitionNodePath == Path(NODE_KEY_ROOT)) {
            Path(this.key)
          } else {
            transitionNodePath append this.key
          }
        } else {
          transitionNodePath.dropLast(1)?.let { it append this.key } ?: Path(this.key)
        }
      }
    }
  }
}

private data class ResolvedTransition(
  val targetPath: Path?,
  val queuedEvents: List<Event>
)

private fun ((ActionEnv<*>) -> Unit).bindTo(env: ActionEnv<*>, parentFlowNodePath: Path): () -> ActionResult {
  return {
    this(env)
    ActionResult(
      events = env.queuedEvents.orEmpty(),
      parentFlowNodePath = parentFlowNodePath,
      updatedState = env.updatedState
    )
  }
}

data class TransitionResult(
  val path: Path,
  val actions: List<(() -> ActionResult)>?,
)

data class ActionResult(
  val events: List<Event>,
  val parentFlowNodePath: Path,
  val updatedState: Any?
)

private fun findEntryNodes(path: Path, newPath: Path): List<Path> {
  val commonPrefix = newPath.findCommonPrefix(path)
  // path:    o.a.b.c
  // newPath: o.a.x.y.z
  //   => [ "o.a.x", "o.a.x.y", "o.a.x.y.z" ]
  //
  // path:    o.a.b.c
  // newPath: x.y.z
  //   => [ "x", "x.y", "x.y.z" ]
  return when {
    path == newPath -> emptyList()
    commonPrefix != null -> {
      val suffix = newPath.drop(commonPrefix.size) ?: error("null suffix")
      suffix.tail?.asIterable()?.scan(commonPrefix.append(suffix.firstSegment)) { acc, key -> acc.append(key) }
        ?: listOf(commonPrefix.append(suffix.firstSegment))
    }
    else -> {
      newPath.tail?.asIterable()
        ?.scan(newPath.take(1)) { acc, key -> acc.append(key) }
        ?: listOf(newPath)
    }
  }
}

private fun findExitNodes(path: Path, newPath: Path): List<Path> {
  // path:    o.a.b.c.d
  // newPath: o.a.x.y.z
  //   => [ "o.a.b.c.d", "o.a.b.c", "o.a.b"  ]
  //
  // path:    o.a.b.c
  // newPath: x.y.z
  //   => [ "o.a.b.c", "o.a.b", "o.a", "o" ]
  return findEntryNodes(newPath, path)
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
 * "flowA.flowB.screenA".findCommonPrefix("flowA") // [ flowA ]
 * "flowA.flowB.screenA".findCommonPrefix("flowA.flowB") // [ flowA, flowB ]
 * "flowA.flowB.screenA".findCommonPrefix("flowC") // [ ]
 * "flowA.flowB.screenA".findCommonPrefix("flowA.flowC") // [ flowA ]
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
private fun FlowNode<*, *>.fullyResolvePath(targetPath: Path): Path {
  return when (val targetNode = this.findChild(targetPath)) {
    is FlowNode<*, *> -> {
      fullyResolvePath(targetPath append targetNode.initial)
    }
    is ScreenNode -> targetPath
    is FinalNode -> targetPath
    null -> error("machine has no node \"$targetPath\"")
  }
}

internal fun FlowNode<*, *>.findNode(path: Path): Node {
  return if (path == Path(NODE_KEY_ROOT)) {
    this
  } else {
    this.findChild(path) ?: error("failed to find node with path=$path")
  }
}

internal fun FlowNode<*, *>.findChild(path: Path): Node? {
  val first = path.firstSegment

  val key = children.keys.find { it == first } ?: return null
  val node = children[key] ?: return null
  val tail = path.tail
  return if (tail != null) {
    when (node) {
      is FlowNode<*, *> -> node.findChild(tail)
      is ScreenNode -> null
      is FinalNode -> null
    }
  } else {
    node
  }
}

/**
 * Finds a first parent node of [path] which is a FlowNode, using receiver object as the root of node hierarchy.
 * If node at [path] is itself a FlowNode, returns it.
 */
private fun FlowNode<*, *>.findAncestorFlowNodePath(path: Path, ignoreCurrent: Boolean = false): Path {
  if (ignoreCurrent && path == Path(NODE_KEY_ROOT)) {
    error("findAncestorFlowNodePath called on root node")
  }
  val path1 = if (ignoreCurrent) {
      path.dropLast(1) ?: Path(NODE_KEY_ROOT)
    } else path
  val nodes = findChildrenAlongPath(path1)
  val list = nodes.dropLastWhile { it !is FlowNode<*, *> }
  return path1.dropLast(nodes.size - list.size) ?: Path(NODE_KEY_ROOT)
}

/**
 * Finds a state of first parent node of [path] which is a FlowNode, using receiver object as the root of node hierarchy.
 * If node at [path] is itself a FlowNode, returns its state.
 */
internal fun FlowNode<*, *>.findAncestorFlowNodeState(path: Path, ignoreCurrent: Boolean = false): Any {
  val ancestorPath = findAncestorFlowNodePath(path, ignoreCurrent)
  return if (ancestorPath == Path(NODE_KEY_ROOT)) {
    this.state
  } else {
    val ancestorNode = findChild(ancestorPath) as FlowNode<Any, *>?
      ?: error("failed to find ancestor flow node for path: $path")
    ancestorNode.state
  }
}

/**
 * Finds a first parent node of [path] which is a FlowNode, using receiver object as the root of node hierarchy.
 * If node at [path] is itself a FlowNode, returns itself.
 */
internal fun FlowNode<*, *>.findAncestorFlowNode(path: Path, ignoreCurrent: Boolean = false): FlowNode<*, *> {
  val ancestorPath = findAncestorFlowNodePath(path, ignoreCurrent)
  return if (ancestorPath == Path(NODE_KEY_ROOT)) {
    this
  } else {
    val ancestorNode = findChild(ancestorPath) as FlowNode<*, *>?
      ?: error("failed to find ancestor flow node for path: $path")
    ancestorNode
  }
}

private fun FlowNode<*, *>.findChildrenAlongPath(path: Path): List<Node> {
  var p: Path? = path
  var n: FlowNode<*, *>? = this

  val nodes = mutableListOf<Node>()

  while (p != null && n != null) {
    val key = n.children.keys.find { it == p!!.firstSegment } ?: return emptyList()
    val node = n.children[key] ?: return emptyList()
    nodes.add(node)
    p = p.tail
    n = when (node) {
      is FlowNode<*, *> -> node
      is ScreenNode -> null
      is FinalNode -> null
    }
  }
  return nodes
}

private val NODE_KEY_ROOT = NodeKey("<root>")

// TODO @Debug @Hooks remove when hooks are added, implement debug log as a hook
private const val DEBUG_LOG_TRANSITIONS = false
