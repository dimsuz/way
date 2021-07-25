package ru.dimsuz.way

import com.github.michaelbull.result.unwrap
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import ru.dimsuz.way.entity.NodeScheme
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.path
import ru.dimsuz.way.entity.scheme
import ru.dimsuz.way.entity.toFlowNode
import ru.dimsuz.way.entity.toService
import ru.dimsuz.way.generator.eventSequence
import ru.dimsuz.way.generator.scheme

class NavigationServiceTest : ShouldSpec({
  context("back stack rules") {
    should("push initial route") {
      val commands = mutableListOf<BackStack>()
      scheme(
        initial = "a",
        node("a")
      ).toCollectingService(commands)

      commands.last().shouldContainExactly(path("a"))
    }

    should("emit nothing if staying on the same node after ignoring event") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b"),
      ).toCollectingService(commands)

      service.sendEvent(Event("UNKNOWN"))

      commands.last().shouldContainExactly(path("a"))
    }

    should("emit nothing if staying on the same node after transition to the same state") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "a")),
        node("b"),
      ).toCollectingService(commands)

      service.sendEvent(Event("T"))

      commands.last().shouldContainExactly(path("a"))
    }

    should("append same level to the back stack") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b", on("U", target = "c")),
        node("c")
      ).toCollectingService(commands)

      service.sendEvent(Event("T"))
      service.sendEvent(Event("U"))

      commands.last().shouldContainExactly(
        path("a"),
        path("b"),
        path("c")
      )
    }

    should("emit pop for transition to the previous screen") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b", on("U", target = "c")),
        node("c", on("BACK", target = "a")),
      ).toCollectingService(commands)

      service.sendEvent(Event("T"))
      service.sendEvent(Event("U"))
      service.sendEvent(Event("BACK"))

      commands.last().shouldContainExactly(
        path("a"),
      )
    }

    should("clear back stack on switching same level flows") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "flowA",
        node(
          "flowA",
          scheme(
            initial = "A1",
            node("A1", on("T", target = "A2")),
            node("A2", on("T", target = "#flowB"))
          )
        ),
        node(
          "flowB",
          scheme(
            initial = "B1",
            node("B1"),
          )
        ),
      ).toCollectingService(commands)

      service.sendEvent(Event("T")) // flowA.A1 -> flowA.A2
      service.sendEvent(Event("T")) // flowA.A2 -> flowB.B1

      commands.last().shouldContainExactly(
        path("flowB.B1")
      )
    }

    should("push without clearing back stack when switching to sub-flows") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "flowA",
        node(
          "flowA",
          scheme(
            initial = "A1",
            node("A1", on("T", target = "A2")),
            node("A2", on("T", target = "flowB")),
            node(
              "flowB",
              scheme(
                initial = "B1",
                node("B1"),
              )
            ),
          )
        ),
      ).toCollectingService(commands)

      service.sendEvent(Event("T")) // flowA.A1 -> flowA.A2
      service.sendEvent(Event("T")) // flowA.A2 -> flowB.B1

      commands.last().shouldContainExactly(
        path("flowA.A1"),
        path("flowA.A2"),
        path("flowA.flowB.B1")
      )
    }

    should("clear back stack when returning back from deeper nested sub-flows") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "flowA",
        node(
          "flowA",
          scheme(
            initial = "A1",
            node("A1", on("T", target = "A2")),
            node("A2", on("T", target = "A3")),
            node("A3", on("T", target = "flowB")),
            node(
              "flowB",
              scheme(
                initial = "B1",
                node("B1", on("T", target = "B2")),
                node(
                  "B2",
                  on("T", target = "flowC"),
                  on("Z", target = "#flowA.A2"),
                ),
                node(
                  "flowC",
                  scheme(
                    initial = "C1",
                    node("C1", on("T", target = "C2")),
                    node("C2", on("T", target = "#flowA.flowB.B2")),
                  )
                )
              )
            ),
          )
        ),
      ).toCollectingService(commands)

      service.sendEvent(Event("T")) // flowA.A1 -> flowA.A2
      service.sendEvent(Event("T")) // flowA.A2 -> flowA.A3
      service.sendEvent(Event("T")) // flowA.A3 -> flowA.flowB.B1
      service.sendEvent(Event("T")) // flowA.flowB.B1 -> flowA.flowB.B2
      service.sendEvent(Event("T")) // flowA.flowB.B2 -> flowA.flowB.flowC.C1
      service.sendEvent(Event("T")) // flowA.flowB.flowC.C1 -> flowA.flowB.flowC.C2
      service.sendEvent(Event("T")) // flowA.flowB.flowC.C2 -> flowA.flowB.B2

      commands.last().shouldContainExactly(
        path("flowA.A1"), path("flowA.A2"), path("flowA.A3"), path("flowA.flowB.B1"), path("flowA.flowB.B2")
      )

      service.sendEvent(Event("Z")) // flowA.flowB.B2 -> flowA.A2

      commands.last().shouldContainExactly(
        path("flowA.A1"), path("flowA.A2")
      )
    }

    should("clear back stack only up to closest common parent") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "flowA",
        node(
          "flowA",
          scheme(
            initial = "A1",
            node("A1", on("T", target = "A2")),
            node("A2", on("T", target = "A3")),
            node("A3", on("T", target = "flowB")),
            node(
              "flowB",
              scheme(
                initial = "B1",
                node("B1", on("T", target = "B2")),
                node(
                  "B2",
                  on("T", target = "#flowA.flowC"),
                ),
              )
            ),
            node(
              "flowC",
              scheme(
                initial = "C1",
                node("C1"),
              )
            )
          )
        ),
      ).toCollectingService(commands)

      service.sendEvent(Event("T")) // flowA.A1 -> flowA.A2
      service.sendEvent(Event("T")) // flowA.A2 -> flowA.A3
      service.sendEvent(Event("T")) // flowA.A3 -> flowA.flowB.B1
      service.sendEvent(Event("T")) // flowA.flowB.B1 -> flowA.flowB.B2
      service.sendEvent(Event("T")) // flowA.flowB.B2 -> flowA.flowC.C1

      // flowA1 and flowA2 are on the same level, so flowB gets cleared, but parent
      // flow's states "flowA.S*" remain in back stack
      commands.last().shouldContainExactly(
        path("flowA.A1"), path("flowA.A2"), path("flowA.A3"), path("flowA.flowC.C1")
      )
    }
  }

  context("entry/exit actions") {
    should("execute on each screen node") {
      val schemeWithEventsGen = Arb.scheme().flatMap { scheme -> Arb.eventSequence(scheme).map { scheme to it } }
      checkAll(iterations = 500, schemeWithEventsGen) { (scheme, events) ->
        // Arrange
        var screenNodeEntryEventCount = 0
        var screenNodeExitEventCount = 0
        val root = scheme
          .toFlowNode<Unit, Unit, Unit>(
            Unit,
            modifyScreenNode = { builder ->
              builder
                .onEntry { screenNodeEntryEventCount++ }
                .onExit { screenNodeExitEventCount++ }
            }
          )
        val service = NavigationService(NavigationMachine(root), { _, _ -> }, { })
        service.start()

        // Act
        events.forEach {
          service.sendEvent(it)
        }

        // Assert
        screenNodeEntryEventCount shouldBeGreaterThan 0
        screenNodeExitEventCount shouldBe screenNodeEntryEventCount - 1
      }
    }

    should("execute number of times corresponding to the number of sent events") {
      // If N events get sent, then this means N transitions will be done (at least in the "generated" scheme case)
      // plus 1 initial transition → N + 1
      val schemeWithEventsGen = Arb.scheme().flatMap { scheme -> Arb.eventSequence(scheme).map { scheme to it } }
      checkAll(iterations = 500, schemeWithEventsGen) { (scheme, events) ->
        // Arrange
        var screenNodeEntryEventCount = 0
        var screenNodeExitEventCount = 0
        val root = scheme
          .toFlowNode<Unit, Unit, Unit>(
            Unit,
            modifyScreenNode = { builder ->
              builder
                .onEntry { screenNodeEntryEventCount++ }
                .onExit { screenNodeExitEventCount++ }
            }
          )
        val service = NavigationService(NavigationMachine(root), { _, _ -> }, { })
        service.start()

        // Act
        events.forEach {
          service.sendEvent(it)
        }

        // Assert
        // number of events sent + initial node entry
        screenNodeEntryEventCount shouldBe events.size + 1
        // not counting exit from the last transitioned-to node, as it didn't happen
        screenNodeExitEventCount shouldBe events.size
      }
    }

    should("execute entry events on each flow node") {
      val schemeWithEventsGen = Arb.scheme().flatMap { scheme -> Arb.eventSequence(scheme).map { scheme to it } }
      checkAll(iterations = 500, schemeWithEventsGen) { (scheme, events) ->
        // Arrange
        var nodeEntryEventCount = 0
        val root = scheme
          .toFlowNode<Unit, Unit, Unit>(
            Unit,
            modifySubFlowNode = { flowNode, builder ->
              builder
                .of(
                  flowNode.newBuilder()
                    .onEntry { nodeEntryEventCount++ }
                    .build(Unit)
                    .unwrap()
                )
                .build()
                .unwrap()
            }
          )
          .newBuilder()
          .onEntry { nodeEntryEventCount++ }
          .build(Unit)
          .unwrap()

        var currentBackStack: BackStack? = null
        val service = NavigationService(NavigationMachine(root), { _, stack -> currentBackStack = stack }, { })
        service.start()

        // Act
        events.forEach {
          service.sendEvent(it)
        }
        // this will bubble up to the very top and transition to final_screen
        service.sendEvent(Event("FINISH"))

        // Assert
        nodeEntryEventCount shouldBe currentBackStack?.last()?.size
      }
    }

    should("execute on each flow node") {
      val schemeWithEventsGen = Arb.scheme().flatMap { scheme -> Arb.eventSequence(scheme).map { scheme to it } }
      checkAll(iterations = 500, schemeWithEventsGen) { (scheme, events) ->
        // Arrange
        var nodeEntryEventCount = 0
        var nodeExitEventCount = 0
        val root = scheme
          .toFlowNode<Unit, Unit, Unit>(
            Unit,
            modifySubFlowNode = { flowNode, builder ->
              builder
                .of(
                  flowNode.newBuilder()
                    .onEntry { nodeEntryEventCount++ }
                    .onExit { nodeExitEventCount++ }
                    .build(Unit)
                    .unwrap()
                )
                .build()
                .unwrap()
            }
          )
          .newBuilder()
          // add a final screen which will cause all child flows to exit
          .addScreenNode(NodeKey("final_screen")) { builder -> builder.build() }
          .on(Event("FINISH")) { navigateTo(NodeKey("final_screen")) }
          .onEntry { nodeEntryEventCount++ }
          .onExit { nodeExitEventCount++ }
          .build(Unit)
          .unwrap()

        val service = NavigationService(NavigationMachine(root), { _, _ -> }, { })
        service.start()

        // Act
        events.forEach {
          service.sendEvent(it)
        }
        // this will bubble up to the very top and transition to final_screen
        service.sendEvent(Event("FINISH"))

        // Assert
        // number of events sent + initial node entry
        nodeEntryEventCount shouldBe events.size + 1
        // not counting exit from the last transitioned-to node, as it didn't happen
        nodeExitEventCount shouldBe events.size
      }
    }
  }
})

private fun NodeScheme.toCollectingService(
  commandSink: MutableList<BackStack>,
  start: Boolean = true,
): NavigationService<BackStack> {
  return this.toService(
    initialState = Unit,
    commandBuilder = { _, new -> new },
    onCommand = { commandSink.add(it) }
  ).apply { if (start) start() }
}

private fun entry(key: String): BackStackEntry {
  return BackStackEntry(NodeKey(key))
}
