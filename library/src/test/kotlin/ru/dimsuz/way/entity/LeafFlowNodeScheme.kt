package ru.dimsuz.way.entity

import com.github.michaelbull.result.unwrap
import com.jakewharton.picnic.table
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeId

data class LeafFlowNodeScheme(
  val initial: NodeId,
  val nodes: Map<NodeId, Map<Event, NodeId>>
) {
  override fun toString(): String {
    return table {
      cellStyle { border = true }
      header {
        row {
          cell("state")
          cell("on event")
          cell("target")
        }
      }
      nodes.forEach { (nodeId, transitions) ->
        row {
          cell(nodeId.id) {
            rowSpan = transitions.size
          }
          if (transitions.isNotEmpty()) {
            cell(transitions.entries.first().key.name)
            cell(transitions.entries.first().value.id)
          } else {
            cell("no transitions")
          }
        }
        transitions.entries.drop(1).forEach { (event, target) ->
          row {
            cell(event.name)
            cell(target.id)
          }
        }
      }
    }.toString()
  }
}

fun on(event: String, target: String): Pair<Event, NodeId> {
  return Event(event) to NodeId(target)
}

fun node(id: String, vararg transition: Pair<Event, NodeId>): Pair<NodeId, Map<Event, NodeId>> {
  return NodeId(id) to transition.toMap()
}

fun nodes(vararg nodes: Pair<NodeId, Map<Event, NodeId>>): Map<NodeId, Map<Event, NodeId>> {
  return nodes.toMap()
}

fun LeafFlowNodeScheme.isValidTransition(prev: NodeId, event: Event, new: NodeId): Boolean {
  val transitions = nodes[prev] ?: return false
  val target = transitions[event]
  return (target != null && target == new) || (target == null && prev == new)
}

fun <S : Any, A : Any, R : Any> LeafFlowNodeScheme.toFlowNode(initialState: S): FlowNode<S, A, R> {
  return FlowNodeBuilder<S, A, R>()
    .setInitial(initial)
    .apply {
      nodes.forEach { (nodeId, transitions) ->
        addScreenNode(nodeId) { builder ->
          transitions.forEach { (event, target) ->
            builder.on(event) { navigateTo(target) }
          }
          builder.build()
        }
      }
    }
    .build(initialState)
    .unwrap()
}
