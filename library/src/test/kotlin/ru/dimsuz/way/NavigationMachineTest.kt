package ru.dimsuz.way

import com.github.michaelbull.result.getError
import com.github.michaelbull.result.unwrap
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import ru.dimsuz.way.entity.NodeScheme
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.path
import ru.dimsuz.way.entity.scheme
import ru.dimsuz.way.entity.toFlowNode
import ru.dimsuz.way.generator.eventSequence
import ru.dimsuz.way.generator.nodeKey
import ru.dimsuz.way.generator.scheme

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
        val scheme = scheme(
          initial = "a",
          node("a", on(event = "T", target = "b")),
          node("b", on(event = "T", target = "c")),
          node("c", on(event = "B", target = "a")),
        )

        runTests(
          scheme,

          send(event = "T", expectPath = "b"),
          send(event = "T", expectPath = "c"),
          send(event = "B", expectPath = "a"),
        )
      }

      should("ignore non-enumerated events") {
        val scheme = scheme(
          initial = "a",
          node("a", on(event = "T", target = "b")),
          node("b", on(event = "B", target = "a")),
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
        val scheme = scheme(
          initial = "a",
          node(
            "a",
            scheme(
              initial = "a2",
              node("a1", on(event = "T", target = "a2")),
              node("a2", on(event = "S", target = "a1")),
            )
          )
        )
        val node = scheme.toFlowNode<Unit, Unit, Unit>(Unit)
        val machine = NavigationMachine(node)

        machine.initial shouldBe path("a.a2")
      }

      should("switch to initial state of compound state 2 levels deep") {
        val scheme = scheme(
          initial = "a",
          node(
            "a",
            scheme(
              initial = "a1",
              node(
                "a1",
                scheme(
                  initial = "a1a",
                  node("a1a"),
                )
              )
            ),
          )
        )
        val node = scheme.toFlowNode<Unit, Unit, Unit>(Unit)
        val machine = NavigationMachine(node)

        machine.initial shouldBe path("a.a1.a1a")
      }

      should("switch to initial state of compound state when switch caused by transition") {
        val scheme = scheme(
          initial = "a",
          node("a", on("T", target = "b")),
          node(
            "b",
            scheme(
              initial = "b1",
              node("b1")
            ),
          )
        )

        runTests(
          scheme,
          send(event = "T", expectPath = "b.b1"),
        )
      }

      should("perform number of distinct transitions equal to number of valid events") {
        val schemeWithEventsGen = Arb.scheme().flatMap { scheme -> Arb.eventSequence(scheme).map { scheme to it } }
        checkAll(config = PropTestConfig(seed = -5110717371309207340), schemeWithEventsGen) { (scheme, events) ->
          val machine = NavigationMachine(scheme.toFlowNode(Unit))
          var currentEventIndex = 0
          val states = mutableListOf<Path>()
          machine.runTransitionSequence(
            nextEventSelector = { events.getOrNull(currentEventIndex) },
            onTransition = { _: Path, _: Event, next: Path ->
              // staying on the same node does not count as a distinct transition,
              // and this shouldn't happen, because [Arb.eventSequence] generates
              // a valid chain of events
              if (states.lastOrNull() != next) {
                states.add(next)
              }
              currentEventIndex += 1
            }
          )

          states.size shouldBe events.size
        }
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
  scheme: NodeScheme,
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
  if (ENABLE_TRANSITION_LOG) {
    println("=== starting transition sequence ===")
  }
  var currentPath = this.initial
  while (true) {
    val nextEvent = nextEventSelector(currentPath)
    if (nextEvent == null) {
      if (ENABLE_TRANSITION_LOG) {
        println("=== ended transition sequence ===")
      }
      return currentPath
    }
    val prev = currentPath
    currentPath = this.transition(currentPath, nextEvent)
    println("$prev x ${nextEvent.name} -> $currentPath")
    onTransition(prev, nextEvent, currentPath)
  }
}

private const val ENABLE_TRANSITION_LOG = false
