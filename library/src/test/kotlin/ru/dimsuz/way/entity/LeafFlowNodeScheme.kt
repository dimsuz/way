package ru.dimsuz.way.entity

import com.jakewharton.picnic.table
import ru.dimsuz.way.Event
import ru.dimsuz.way.NodeId

data class LeafFlowNodeScheme(
  val states: Map<NodeId, Map<Event, NodeId>>
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
      states.forEach { (nodeId, transitions) ->
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

fun LeafFlowNodeScheme.isValidTransition(prev: NodeId, event: Event, new: NodeId): Boolean {
  val transitions = states[prev] ?: return false
  val target = transitions[event]
  return (target != null && target == new) || (target == null && prev == new)
}
