package ru.dimsuz.way.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import ru.dimsuz.way.Event
import ru.dimsuz.way.NodeId
import ru.dimsuz.way.entity.LeafFlowNodeScheme

fun Arb.Companion.leafFlowNodeScheme(): Arb<LeafFlowNodeScheme> {
  return arbitrary { rs ->
    val stateNodes = Arb.list(Arb.nodeId(), 1..20).next(rs)
    val states = stateNodes
      .associateWith {
        val events = Arb.list(Arb.event(), 0..stateNodes.size).next(rs)
        val targets = stateNodes.shuffled(rs.random)
        events.mapIndexed { i, event ->
          event to targets[i]
        }.toMap()
      }
    LeafFlowNodeScheme(states)
  }
}

/**
 * Produces random event selection for each state
 */
fun Arb.Companion.screenEvents(scheme: LeafFlowNodeScheme): Arb<Map<NodeId, Event?>> {
  return arbitrary { rs ->
    scheme.states.mapValues { (_, transitions) ->
      if (transitions.isNotEmpty()) {
        Arb.element(transitions.keys).orNull(nullProbability = 0.3).next(rs)
      } else null
    }
  }
}