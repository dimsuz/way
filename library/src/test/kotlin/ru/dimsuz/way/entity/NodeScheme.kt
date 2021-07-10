package ru.dimsuz.way.entity

import com.github.michaelbull.result.unwrap
import com.jakewharton.picnic.table
import ru.dimsuz.way.CommandBuilder
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NavigationMachine
import ru.dimsuz.way.NavigationService
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.Path

data class NodeScheme(
  val initial: String,
  val nodes: Map<String, SchemeNode>
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
      nodes.forEach { (nodeKey, node) ->
        if (node is SchemeNode.Compound) {
          row {
            cell("<compound node>")
          }
        } else {
          val transitions = (node as SchemeNode.Atomic).transitions
          row {
            cell(nodeKey) {
              rowSpan = transitions.size
            }
            if (transitions.isNotEmpty()) {
              cell(transitions.entries.first().key.name)
              cell(transitions.entries.first().value.key)
            } else {
              cell("no transitions")
            }
          }
          transitions.entries.drop(1).forEach { (event, target) ->
            row {
              cell(event.name)
              cell(target.key)
            }
          }
        }
      }
    }.toString()
  }
}

sealed class SchemeNode {
  data class Atomic(val transitions: Map<Event, NodeKey>) : SchemeNode()
  data class Compound(val scheme: NodeScheme) : SchemeNode()
}

fun on(event: String, target: String): Pair<Event, NodeKey> {
  return Event(event) to NodeKey(target)
}

fun node(id: String, vararg transition: Pair<Event, NodeKey>): Pair<String, SchemeNode> {
  return id to SchemeNode.Atomic(transition.toMap())
}

fun node(id: String, child: NodeScheme): Pair<String, SchemeNode> {
  return id to SchemeNode.Compound(child)
}

fun scheme(initial: String, vararg nodes: Pair<String, SchemeNode>): NodeScheme {
  return NodeScheme(initial, nodes.toMap())
}

fun <S : Any, A : Any, R : Any> NodeScheme.toFlowNode(initialState: S): FlowNode<S, A, R> {
  return FlowNodeBuilder<S, A, R>()
    .setInitial(NodeKey(initial))
    .apply {
      nodes.forEach { (nodeKey, node) ->
        when (node) {
          is SchemeNode.Atomic -> {
            addScreenNode(NodeKey(nodeKey)) { builder ->
              node.transitions.forEach { (event, target) ->
                builder.on(event) {
                  if (target.key.contains(".")) {
                    val segments = target.key.split(".").map { NodeKey(it) }
                    navigateTo(Path(segments.first(), segments.drop(1)))
                  } else {
                    navigateTo(target)
                  }
                }
              }
              builder.build()
            }
          }
          is SchemeNode.Compound -> {
            addFlowNode<Any>(NodeKey(nodeKey)) { builder ->
              builder.of(
                node.scheme.toFlowNode<Any, Any, Any>(Unit)
              ).build().unwrap()
            }
          }
        }
      }
    }
    .build(initialState)
    .unwrap()
}

fun <S : Any, C : Any> NodeScheme.toService(
  initialState: S,
  commandBuilder: CommandBuilder<C>,
  onCommand: (command: C) -> Unit
): NavigationService<C> {
  return NavigationService(NavigationMachine(this.toFlowNode(initialState)), commandBuilder, onCommand)
}
