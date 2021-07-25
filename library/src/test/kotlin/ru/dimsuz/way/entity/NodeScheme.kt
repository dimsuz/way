package ru.dimsuz.way.entity

import com.github.michaelbull.result.unwrap
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.table
import ru.dimsuz.way.CommandBuilder
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NavigationMachine
import ru.dimsuz.way.NavigationService
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.Path
import ru.dimsuz.way.ScreenNodeBuilder
import ru.dimsuz.way.SubFlowBuilder

data class NodeScheme(
  val initial: String,
  val nodes: Map<String, SchemeNode>
) {

  override fun toString(): String {
    return table {
      cellStyle { border = true; paddingLeft = 1; paddingRight = 1 }
      header {
        row {
          cell("STATE")
          cell("ON EVENT")
          cell("TARGET")
        }
      }
      row {
        cell("initial")
        cell(initial) {
          columnSpan = 2
        }
      }
      nodes(level = 0, nodes)
    }.toString()
  }

  private fun TableSectionDsl.nodes(level: Int, nodes: Map<String, SchemeNode>) {
    nodes.forEach { (nodeKey, node) ->
      if (node is SchemeNode.Compound) {
        row {
          repeat(level) { cell("") }
          cell(nodeKey)
          cell("COMPOUND NODE") {
            columnSpan = level + 3
          }
        }
        row {
          repeat(level + 1) { cell("") { borderBottom = false } }
          cell("initial")
          cell(node.scheme.initial) {
            columnSpan = level + 2
          }
        }
        nodes(level + 1, node.scheme.nodes)
      } else {
        val transitions = (node as SchemeNode.Atomic).transitions
        row {
          repeat(level) { cell("") { borderTop = false; borderBottom = false } }
          cell(nodeKey) {
            rowSpan = transitions.size
          }
          if (transitions.isNotEmpty()) {
            cell(transitions.entries.first().key.name)
            cell(transitions.entries.first().value.key)
          } else {
            cell("no transitions") {
              this.columnSpan = 2
            }
          }
        }
        transitions.entries.drop(1).forEach { (event, target) ->
          row {
            repeat(level) { cell("") { borderTop = false; borderBottom = false } }
            cell(event.name)
            cell(target.key)
          }
        }
      }
    }
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

fun path(s: String): Path {
  val segments = s.split(".").map { NodeKey(it) }
  return Path(segments.first(), segments.drop(1))
}

fun <S : Any, A : Any, R : Any> NodeScheme.toFlowNode(
  initialState: S,
  modifyScreenNode: ((ScreenNodeBuilder<*, *, *>) -> Unit)? = null,
  modifySubFlowNode: ((FlowNode<Any, Any, Any>, SubFlowBuilder<*, *, *, Any>) -> Unit)? = null,
): FlowNode<S, A, R> {
  return FlowNodeBuilder<S, A, R>()
    .setInitial(NodeKey(initial))
    .apply {
      nodes.forEach { (nodeKey, node) ->
        when (node) {
          is SchemeNode.Atomic -> {
            addScreenNode(NodeKey(nodeKey)) { builder ->
              node.transitions.forEach { (event, target) ->
                builder.on(event) {
                  when {
                    target.key.startsWith("#") -> {
                      val segments = target.key.drop(1).split(".").filterNot { it.isEmpty() }.map { NodeKey(it) }
                      navigateTo(Path(segments.first(), segments.drop(1)))
                    }
                    target.key.contains(".") -> {
                      val segments = target.key.split(".").map { NodeKey(it) }
                      navigateTo(Path(segments.first(), segments.drop(1)))
                    }
                    else -> {
                      navigateTo(target)
                    }
                  }
                }
              }
              modifyScreenNode?.invoke(builder)
              builder.build()
            }
          }
          is SchemeNode.Compound -> {
            addFlowNode<Any>(NodeKey(nodeKey)) { builder ->
              val flow = node.scheme.toFlowNode<Any, Any, Any>(Unit, modifyScreenNode, modifySubFlowNode)
              builder
                .of(flow)
                .apply { modifySubFlowNode?.invoke(flow, this) }
                .build()
                .unwrap()
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
