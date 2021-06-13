package ru.dimsuz.way.entity

import com.github.michaelbull.result.unwrap
import com.jakewharton.picnic.table
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey

data class LeafFlowNodeScheme(
  val initial: NodeKey,
  val nodes: Map<NodeKey, Map<Event, NodeKey>>
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
      nodes.forEach { (nodeKey, transitions) ->
        row {
          cell(nodeKey.id) {
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

fun on(event: String, target: String): Pair<Event, NodeKey> {
  return Event(event) to NodeKey(target)
}

fun node(id: String, vararg transition: Pair<Event, NodeKey>): Pair<NodeKey, Map<Event, NodeKey>> {
  return NodeKey(id) to transition.toMap()
}

fun nodes(vararg nodes: Pair<NodeKey, Map<Event, NodeKey>>): Map<NodeKey, Map<Event, NodeKey>> {
  return nodes.toMap()
}

fun LeafFlowNodeScheme.isValidTransition(prev: NodeKey, event: Event, new: NodeKey): Boolean {
  val transitions = nodes[prev] ?: return false
  val target = transitions[event]
  return (target != null && target == new) || (target == null && prev == new)
}

fun <S : Any, A : Any, R : Any> LeafFlowNodeScheme.toFlowNode(initialState: S): FlowNode<S, A, R> {
  return FlowNodeBuilder<S, A, R>()
    .setInitial(initial)
    .apply {
      nodes.forEach { (nodeKey, transitions) ->
        addScreenNode(nodeKey) { builder ->
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
