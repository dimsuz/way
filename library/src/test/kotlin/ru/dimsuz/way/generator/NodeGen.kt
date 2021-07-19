package ru.dimsuz.way.generator

import io.kotest.property.Arb
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.ListShrinker
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import ru.dimsuz.way.Event
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.entity.NodeScheme
import ru.dimsuz.way.entity.SchemeNode
import java.util.Locale
import kotlin.random.nextInt

fun Arb.Companion.scheme(maxLevel: Int = 3): Arb<NodeScheme> {
  return arbitrary(shrinker = SchemeShrinker()) { rs ->
    val nodes = Arb.schemeNodes(maxLevel).next(rs)
    NodeScheme(
      initial = Arb.element(nodes.keys).next(rs),
      nodes = nodes
    )
  }
}

/**
 * Generates a sequence of events which represent a valid set of transitions.
 * Events are selected so that each of them will be valid for the node which is reached
 * by sending a sequence of events preceding the current one.
 * That implies that each event will transition to a new node, never staying on the same one.
 */
fun Arb.Companion.eventSequence(scheme: NodeScheme): Arb<List<Event>> {
  return arbitrary(shrinker = ListShrinker(1..50)) { rs ->
    val count = rs.random.nextInt(1..50)
    val events = mutableListOf<Event>()
    var containingNode = SchemeNode.Compound(scheme)
    var currentNode = scheme.nodes[scheme.initial]
    while (events.size < count) {
      when (currentNode) {
        is SchemeNode.Atomic -> {
          if (currentNode.transitions.isEmpty()) {
            break
          }
          val transition = Arb.element(currentNode.transitions.entries).next(rs)
          events.add(transition.key)
          currentNode = containingNode.scheme.nodes[transition.value.key]
            ?: error("failed to find node '${transition.value.key}' in the containing node")
        }
        is SchemeNode.Compound -> {
          containingNode = currentNode
          currentNode = currentNode.scheme.nodes[currentNode.scheme.initial]
            ?: error("no initial node definition found for '${currentNode.scheme.initial}'")
        }
      }
    }
    events
  }
}

private fun Arb.Companion.schemeNodes(maxLevel: Int): Arb<Map<String, SchemeNode>> {
  return arbitrary { rs ->
    val nodeKeys = Arb.list(Arb.nodeKeyName(), range = 1..30).next(rs)
    val atomicNodes = mutableMapOf<String, NodeTransitions>()
    val compoundNodeKeys = if (maxLevel > 0 && nodeKeys.size >= 3) {
      nodeKeys.take(rs.random.nextInt(0, nodeKeys.size / 3))
    } else emptyList()
    val atomicNodeKeys = nodeKeys.minus(compoundNodeKeys)
    atomicNodeKeys.forEach { nodeKey ->
      if (ENABLE_SCHEME_GEN_DEBUG) {
        println("generating transitions for node $nodeKey")
      }
      // better have more states than events, otherwise it would be hard to
      // find a combination of transitions which doesn't introduce a cycle
      val events = Arb.list(Arb.eventName(), range = 0..nodeKeys.size / 2).next(rs)
      val potentialTargets = nodeKeys.minus(nodeKey)
      val transitions = mutableMapOf<String, String>()
      var attemptsRemaining = 15
      for (event in events) {
        while (attemptsRemaining > 0) {
          val target = Arb.element(potentialTargets).next(rs)
          transitions[event] = target
          if (hasCycle(atomicNodes, nodeKey, transitions)) {
            transitions.remove(event)
            attemptsRemaining -= 1
          } else break
        }
        if (attemptsRemaining <= 0) {
          if (ENABLE_SCHEME_GEN_DEBUG) {
            println("attempts exhausted while trying to find non-cycling targets for node $nodeKey, moving on")
          }
          break
        }
      }
      atomicNodes[nodeKey] = transitions
    }
    atomicNodes
      .mapValues { (_, transitions) ->
        SchemeNode.Atomic(transitions = transitions.entries.associate { Event(it.key) to NodeKey(it.value) })
      }
      .plus(
        compoundNodeKeys.map { key ->
          key to SchemeNode.Compound(Arb.scheme(maxLevel - 1).next(rs))
        }
      )
  }
}

typealias NodeTransitions = Map<String, String>

private fun hasCycle(nodes: Map<String, NodeTransitions>, newKey: String, newKeyTransitions: NodeTransitions): Boolean {
  val newNodes = nodes.plus(newKey to newKeyTransitions)
  val adjacent = newNodes.mapValues { (_, transitions) -> transitions.values }
  val cycle = dfsCycleSearch(adjacent)
  return if (cycle != null) {
    if (ENABLE_SCHEME_GEN_DEBUG) {
      println("cycle detected in nodes ${cycle.first} → ${cycle.second}")
    }
    true
  } else false
}

private fun dfsCycleSearch(adjacent: Map<String, Collection<String>>): Pair<String, String>? {
  val discovered = mutableSetOf<String>()
  val finished = mutableSetOf<String>()

  fun dfsVisit(key: String): Pair<String, String>? {
    discovered.add(key)

    adjacent[key].orEmpty().forEach { key1 ->
      if (key1 in discovered) {
        return key to key1
      }
      if (key1 !in finished) {
        dfsVisit(key1)
      }
    }

    discovered.remove(key)
    finished.add(key)

    return null
  }

  adjacent.keys.forEach { key ->
    if (key !in discovered && key !in finished) {
      val cycle = dfsVisit(key)
      if (cycle != null) {
        return cycle
      }
    }
  }

  return null
}

private fun Arb.Companion.nodeKeyName() = Arb.string(5..8, Arb.az())
private fun Arb.Companion.eventName() = Arb.string(5..8, Arb.az()).map { it.uppercase(Locale.getDefault()) }

fun Arb.Companion.nodeKey(): Arb<NodeKey> {
  return Arb.nodeKeyName().map { NodeKey(it) }
}

fun Arb.Companion.event(): Arb<Event> {
  return Arb.eventName().map { Event(it) }
}

private class SchemeShrinker : Shrinker<NodeScheme> {
  override fun shrink(value: NodeScheme): List<NodeScheme> {
    return when {
      value.nodes.isEmpty() -> emptyList()
      value.nodes.size == 1 -> emptyList()
      else -> {
        listOfNotNull(
          // only the initial
          value.nodes.filter { it.key == value.initial },
          // first non-initial dropped
          value.nodes.minus(value.nodes.keys.first { it != value.initial }),
          // last non-initial dropped
          value.nodes.minus(value.nodes.keys.last { it != value.initial }),
          // half of the nodes
          value.nodes.minus(value.nodes.keys.toList().filter { it != value.initial }.dropLast(value.nodes.size / 2)),
        ).map { NodeScheme(value.initial, it) }
      }
    }
  }
}

private const val ENABLE_SCHEME_GEN_DEBUG = false
