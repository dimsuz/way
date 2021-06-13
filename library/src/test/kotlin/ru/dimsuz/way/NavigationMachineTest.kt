package ru.dimsuz.way

import com.github.michaelbull.result.getError
import com.github.michaelbull.result.unwrap
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import ru.dimsuz.way.entity.LeafFlowNodeScheme
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.nodes
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.toFlowNode
import ru.dimsuz.way.generator.nodeKey

class NavigationMachineTest : ShouldSpec({
  context("initial state") {
    should("report error if initial state is missing") {
      val screen = Arb.nodeKey().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .addScreenNode(screen) { builder -> builder.build() }
        .build(Unit)

      node.getError() shouldBe FlowNodeBuilder.Error.MissingInitialNode
    }

    should("switch to initial state") {
      val screen = Arb.nodeKey().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .setInitial(screen)
        .addScreenNode(screen) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      val machine = NavigationMachine<Unit, Unit, Unit>(node)

      machine.initialNodeKey shouldBe screen
    }
  }

  context("transitions") {
    context("atomic") {
      should("perform simple transitions") {
        val scheme = LeafFlowNodeScheme(
          initial = NodeKey("a"),
          nodes = nodes(
            node("a", on(event = "T", target = "b")),
            node("b", on(event = "T", target = "c")),
            node("c", on(event = "B", target = "a")),
          )
        )

        runTests(
          scheme,

          send(event = "T", expectNode = "b"),
          send(event = "T", expectNode = "c"),
          send(event = "B", expectNode = "a"),
        )
      }

      should("ignore non-enumerated events") {
        val scheme = LeafFlowNodeScheme(
          initial = NodeKey("a"),
          nodes = nodes(
            node("a", on(event = "T", target = "b")),
            node("b", on(event = "B", target = "a")),
          )
        )

        runTests(
          scheme,

          send(event = "T", expectNode = "b"),
          send(event = "T", expectNode = "b"),
          send(event = "X", expectNode = "b"),
        )
      }
    }
    context("compound") {
      should("switch to initial state of compound state") {
        val scheme = LeafFlowNodeScheme(
          initial = NodeKey("a2"),
          nodes = nodes(
            node("a1", on(event = "T", target = "a2")),
            node("a2", on(event = "S", target = "a1")),
          )
        )
        val node = FlowNodeBuilder<Unit, Unit, Unit>()
          .setInitial(NodeKey("a"))
          .addFlowNode<Unit>(NodeKey("a")) { builder ->
            builder
              .of(scheme.toFlowNode<Unit, Unit, Unit>(Unit))
              .build()
          }
          .build(Unit)
          .unwrap()
        val machine = NavigationMachine(node)

        // TODO сделать так: initialNodeId -> rename initial && initial is Path == true &&
        //  initial shouldBe Path.of(NodeId("a"),NodeId("a2"))
        machine.initialNodeKey shouldBe NodeKey("a2")
      }
    }
  }
})

private data class TestTransition(
  val event: Event,
  val expectedNode: NodeKey,
)

private fun send(event: String, expectNode: String): TestTransition {
  return TestTransition(Event(event), NodeKey(expectNode))
}

private fun runTests(
  scheme: LeafFlowNodeScheme,
  vararg expectations: TestTransition
) {
  val machine = NavigationMachine(scheme.toFlowNode(Unit))
  var currentEventIndex = 0
  machine.runTransitionSequence(
    nextEventSelector = { expectations.getOrNull(currentEventIndex)?.event },
    onTransition = { prev: NodeKey, event: Event, next: NodeKey ->
      withClue("prev = $prev, event = $event, next = $next") {
        val expected = expectations[currentEventIndex].expectedNode
        next shouldBe expected
      }
      currentEventIndex += 1
    }
  )
}

private fun <S : Any, A : Any, R : Any> NavigationMachine<S, A, R>.runTransitionSequence(
  nextEventSelector: (node: NodeKey) -> Event?,
  onTransition: (prev: NodeKey, event: Event, next: NodeKey) -> Unit
): NodeKey {
  var currentNode = this.initialNodeKey
  while (true) {
    val nextEvent = nextEventSelector(currentNode) ?: return currentNode
    val prev = currentNode
    currentNode = this.transition(currentNode, nextEvent)
    println("${prev.id} x ${nextEvent.name} -> ${currentNode.id}")
    onTransition(prev, nextEvent, currentNode)
  }
}
