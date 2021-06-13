package ru.dimsuz.way.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import ru.dimsuz.way.Event
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.entity.LeafFlowNodeScheme

fun Arb.Companion.leafFlowNodeScheme(): Arb<LeafFlowNodeScheme> {
  return arbitrary { rs ->
    val stateNodes = Arb.list(Arb.nodeKey(), 1..20).next(rs)
    val states = stateNodes
      .associateWith { stateNode ->
        val targets = stateNodes.filter { stateNode != it }.shuffled(rs.random)
        val events = Arb.list(Arb.event(), 0..targets.size).next(rs)
        events.mapIndexed { i, event ->
          event to targets[i]
        }.toMap()
      }
    LeafFlowNodeScheme(initial = stateNodes.first(), states)
  }
}

/**
 * Produces random event selection for each state
 */
fun Arb.Companion.screenEvents(scheme: LeafFlowNodeScheme): Arb<Map<NodeKey, Event?>> {
  return arbitrary { rs ->
    scheme.nodes.mapValues { (_, transitions) ->
      if (transitions.isNotEmpty()) {
        Arb.element(transitions.keys).orNull(nullProbability = 0.3).next(rs)
      } else null
    }
  }
}
