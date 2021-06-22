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

      val machine = NavigationMachine(node)

      machine.initial shouldBe Path(screen)
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

          send(event = "T", expectPath = "b"),
          send(event = "T", expectPath = "c"),
          send(event = "B", expectPath = "a"),
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

          send(event = "T", expectPath = "b"),
          send(event = "T", expectPath = "b"),
          send(event = "X", expectPath = "b"),
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
              .unwrap()
          }
          .build(Unit)
          .unwrap()
        val machine = NavigationMachine(node)

        machine.initial shouldBe (NodeKey("a") append NodeKey("a2"))
      }

      should("switch to initial state of compound state 2 levels deep") {
        val scheme2 = LeafFlowNodeScheme(
          initial = NodeKey("a1a"),
          nodes = nodes(
            node("a1a"),
          )
        )
        val node = FlowNodeBuilder<Unit, Unit, Unit>()
          .setInitial(NodeKey("a"))
          .addFlowNode<Unit>(NodeKey("a")) { builder ->
            builder
              .of(
                FlowNodeBuilder<Unit, Unit, Unit>()
                  .setInitial(NodeKey("a1"))
                  .addFlowNode<Unit>(NodeKey("a1")) { childBuilder ->
                    childBuilder.of(scheme2.toFlowNode<Unit, Unit, Unit>(Unit))
                      .build()
                      .unwrap()
                  }
                  .build(Unit)
                  .unwrap()
              )
              .build()
              .unwrap()
          }
          .build(Unit)
          .unwrap()
        val machine = NavigationMachine(node)

        machine.initial shouldBe (NodeKey("a") append NodeKey("a1") append NodeKey("a1a"))
      }
    }
  }
})

private data class TestTransition(
  val event: Event,
  val expectedPath: Path,
)

private fun send(event: String, expectPath: String): TestTransition {
  return TestTransition(Event(event), Path.fromNonEmptyListOf(expectPath.split(".").map { NodeKey(it) }))
}

private fun runTests(
  scheme: LeafFlowNodeScheme,
  vararg expectations: TestTransition
) {
  val machine = NavigationMachine(scheme.toFlowNode(Unit))
  var currentEventIndex = 0
  machine.runTransitionSequence(
    nextEventSelector = { expectations.getOrNull(currentEventIndex)?.event },
    onTransition = { prev: Path, event: Event, next: Path ->
      withClue("prev = $prev, event = $event, next = $next") {
        val expected = expectations[currentEventIndex].expectedPath
        next shouldBe expected
      }
      currentEventIndex += 1
    }
  )
}

private fun <S : Any, A : Any, R : Any> NavigationMachine<S, A, R>.runTransitionSequence(
  nextEventSelector: (path: Path) -> Event?,
  onTransition: (prev: Path, event: Event, next: Path) -> Unit
): Path {
  var currentPath = this.initial
  while (true) {
    val nextEvent = nextEventSelector(currentPath) ?: return currentPath
    val prev = currentPath
    currentPath = this.transition(currentPath, nextEvent)
    println("$prev x ${nextEvent.name} -> $currentPath")
    onTransition(prev, nextEvent, currentPath)
  }
}
