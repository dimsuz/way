package ru.dimsuz.way

import com.github.michaelbull.result.unwrap
import io.kotest.core.config.EmptyExtensionRegistry.isNotEmpty
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropertyTesting
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import ru.dimsuz.way.Event.Name
import ru.dimsuz.way.entity.NodeScheme
import ru.dimsuz.way.entity.SchemeNode
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.path
import ru.dimsuz.way.entity.scheme
import ru.dimsuz.way.entity.toFlowNode
import ru.dimsuz.way.entity.toService
import ru.dimsuz.way.generator.eventSequence
import ru.dimsuz.way.generator.scheme
import ru.dimsuz.way.generator.schemeWithEventSequence

class NavigationServiceTest : ShouldSpec({
  PropertyTesting.shouldPrintShrinkSteps = false

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

      service.sendEvent(Event(Name("UNKNOWN")))

      commands.last().shouldContainExactly(path("a"))
    }

    should("emit nothing if staying on the same node after transition to the same state") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "a")),
        node("b"),
      ).toCollectingService(commands)

      service.sendEvent(Event(Name("T")))

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

      service.sendEvent(Event(Name("T")))
      service.sendEvent(Event(Name("U")))

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

      service.sendEvent(Event(Name("T")))
      service.sendEvent(Event(Name("U")))
      service.sendEvent(Event(Name("BACK")))

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

      service.sendEvent(Event(Name("T"))) // flowA.A1 -> flowA.A2
      service.sendEvent(Event(Name("T"))) // flowA.A2 -> flowB.B1

      commands.last().shouldContainExactly(
        path("flowB.B1")
      )
    }

    should("clear back stack on switching same level flow to screen") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "flowA",
        node(
          "flowA",
          scheme(
            initial = "A1",
            node("A1", on("T", target = "#F1")),
          )
        ),
        node(
          "F1",
        ),
      ).toCollectingService(commands)

      service.sendEvent(Event(Name("T"))) // flowA.A1 -> F1

      commands.last().shouldContainExactly(
        path("F1")
      )
    }

    should("clear back stack on switching same level flow to screen in nested case") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "flowX",
        node(
          "flowX",
          scheme(
            initial = "flowA",
            node(
              "flowA",
              scheme(
                initial = "A1",
                node("A1", on("T", target = "#flowX.F1")),
              )
            ),
            node(
              "F1",
            ),
          )
        ),
      ).toCollectingService(commands)

      service.sendEvent(Event(Name("T"))) // flowA.A1 -> F1

      commands.last().shouldContainExactly(
        path("flowX.F1")
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

      service.sendEvent(Event(Name("T"))) // flowA.A1 -> flowA.A2
      service.sendEvent(Event(Name("T"))) // flowA.A2 -> flowB.B1

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

      service.sendEvent(Event(Name("T"))) // flowA.A1 -> flowA.A2
      service.sendEvent(Event(Name("T"))) // flowA.A2 -> flowA.A3
      service.sendEvent(Event(Name("T"))) // flowA.A3 -> flowA.flowB.B1
      service.sendEvent(Event(Name("T"))) // flowA.flowB.B1 -> flowA.flowB.B2
      service.sendEvent(Event(Name("T"))) // flowA.flowB.B2 -> flowA.flowB.flowC.C1
      service.sendEvent(Event(Name("T"))) // flowA.flowB.flowC.C1 -> flowA.flowB.flowC.C2
      service.sendEvent(Event(Name("T"))) // flowA.flowB.flowC.C2 -> flowA.flowB.B2

      commands.last().shouldContainExactly(
        path("flowA.A1"), path("flowA.A2"), path("flowA.A3"), path("flowA.flowB.B1"), path("flowA.flowB.B2")
      )

      service.sendEvent(Event(Name("Z"))) // flowA.flowB.B2 -> flowA.A2

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

      service.sendEvent(Event(Name("T"))) // flowA.A1 -> flowA.A2
      service.sendEvent(Event(Name("T"))) // flowA.A2 -> flowA.A3
      service.sendEvent(Event(Name("T"))) // flowA.A3 -> flowA.flowB.B1
      service.sendEvent(Event(Name("T"))) // flowA.flowB.B1 -> flowA.flowB.B2
      service.sendEvent(Event(Name("T"))) // flowA.flowB.B2 -> flowA.flowC.C1

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
          .toFlowNode<Unit, Unit>(
            Unit,
            modifyScreenNode = { builder ->
              builder
                .onEntry { screenNodeEntryEventCount++ }
                .onExit { screenNodeExitEventCount++ }
            }
          )
        val service = NavigationService(NavigationMachine(root), { _, _, _ -> }, { })
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
          .toFlowNode<Unit, Unit>(
            Unit,
            modifyScreenNode = { builder ->
              builder
                .onEntry { screenNodeEntryEventCount++ }
                .onExit { screenNodeExitEventCount++ }
            }
          )
        val service = NavigationService(NavigationMachine(root), { _, _, _ -> }, { })
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
      checkAll(iterations = 500, Arb.schemeWithEventSequence()) { (scheme, events) ->
        // Arrange
        var nodeEntryEventCount = 0
        val root = scheme
          .toFlowNode<Unit, Unit>(
            Unit,
            modifySubFlow = { builder ->
              builder.onEntry { nodeEntryEventCount++ }
            }
          )
          .newBuilder()
          .onEntry {
            nodeEntryEventCount++
          }
          .build(Unit)
          .unwrap()

        var currentBackStack: BackStack? = null
        val service = NavigationService(NavigationMachine(root), { _, stack, _ -> currentBackStack = stack }, { })
        service.start()

        // Act
        events.forEach {
          service.sendEvent(it)
        }

        // Assert
        try {
          nodeEntryEventCount shouldBe currentBackStack?.last()?.size
        } catch (e: Throwable) {
          println(scheme.toTableString())
          println(events)
          throw e
        }
      }
    }

    should("execute entry/exit on each flow node") {
      val schemeGen =
        Arb.schemeWithEventSequence().filter { (scheme, _) -> scheme.nodes.any { it.value is SchemeNode.Compound } }
      checkAll(iterations = 500, schemeGen) { (scheme, events) ->
        // Arrange
        var nodeEntryEventCount = 0
        var nodeExitEventCount = 0
        val root = scheme
          .toFlowNode<Unit, Unit>(
            Unit,
            modifySubFlow = { builder ->
              builder
                .onEntry { nodeEntryEventCount++ }
                .onExit { nodeExitEventCount++ }
            }
          )
          .newBuilder()
          // add a final screen which will cause all child flows to exit
          .addScreenNode(NodeKey("final_screen")) { builder -> builder.build() }
          .on(Name("FINISH")) { navigateTo(NodeKey("final_screen")) }
          .build(Unit)
          .unwrap()

        val service = NavigationService(NavigationMachine(root), { _, _, _ -> }, { })
        service.start()

        // Act
        events.forEach {
          service.sendEvent(it)
        }
        // this will bubble up to the very top and transition to final_screen
        service.sendEvent(Event(Name("FINISH")))

        // Assert
        try {
          nodeExitEventCount shouldBe nodeEntryEventCount
        } catch (e: Throwable) {
          println(scheme.toTableString())
          println(events)
          throw e
        }
      }
    }

    should("send events emitted in entry/exit actions on root node") {
      val commands = mutableListOf<BackStack>()
      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("a1"))
        .onEntry {
          sendEvent(Event(Name("T")))
        }
        .on(Name("T")) { navigateTo(NodeKey("a2")) }
        .addScreenNode(NodeKey("a1")) { builder -> builder.build() }
        .addScreenNode(NodeKey("a2")) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      node.toCollectingService(commands)

      commands.last().shouldContainExactly(path("a1"), path("a2"))
    }

    should("send events emitted in entry/exit actions on child flow node") {
      val commands = mutableListOf<BackStack>()
      val flowB = FlowNodeBuilder<Unit, String>()
        .setInitial(NodeKey("b1"))
        .onEntry {
          sendEvent(Event(Name("T")))
        }
        .addScreenNode(NodeKey("b1")) { sb -> sb.build() }
        .addScreenNode(NodeKey("b2")) { sb -> sb.build() }
        .on(Name("T")) { navigateTo(NodeKey("b2")) }
        .build(Unit)
        .unwrap()
      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowB"))
        .addFlowNode<String>(NodeKey("flowB")) { builder ->
          builder
            .of(flowB)
            .build()
            .unwrap()
        }
        .build(Unit)
        .unwrap()

      node.toCollectingService(commands)

      commands.last().shouldContainExactly(path("flowB.b1"), path("flowB.b2"))
    }
  }

  context("node actions") {
    should("send events from within the screen onEntry action") {
      val commands = mutableListOf<BackStack>()
      scheme(
        initial = "a",
        node("a", on("EN", target = "b"), on("EX", target = "c")),
        node("b"),
      )
        .toFlowNode<Unit, Unit>(
          initialState = Unit,
          modifyScreenNode = { builder ->
            builder.onEntry { sendEvent(Event(Name("EN"))) }
          }
        )
        .toCollectingService(commands)

      commands.last().shouldContainExactly(path("a"), path("b"))
    }

    should("send events from within the screen onExit action") {
      val commands = mutableListOf<BackStack>()
      val service = scheme(
        initial = "a",
        node("a", on("EN", target = "b")),
        node("b", on("EX", target = "c")),
        node("c"),
      )
        .toFlowNode<Unit, Unit>(
          initialState = Unit,
          modifyScreenNode = { builder ->
            builder.onExit { sendEvent(Event(Name("EX"))) }
          }
        )
        .toCollectingService(commands)

      // currently, on "a". EN → move to "b" and cause a.onExit to fire. Which will send "EX" and move to "c"
      service.sendEvent(Event(Name("EN")))

      commands.last().shouldContainExactly(path("a"), path("b"), path("c"))
    }
  }

  context("transition actions") {
    should("queue events specified in action block") {
      val commands = mutableListOf<BackStack>()
      val service = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("a"))
        .addScreenNode(NodeKey("a")) { builder ->
          builder
            .on(Name("T")) {
              action {
                sendEvent(Event(Name("Y")))
              }
            }
            .build()
        }
        .addScreenNode(NodeKey("b")) { builder -> builder.build() }
        .on(Name("Y")) {
          navigateTo(NodeKey("b"))
        }
        .build(Unit)
        .unwrap()
        .toCollectingService(commands)

      service.sendEvent(Event(Name("T")))

      commands.last().shouldContainExactly(path("a"), path("b"))
    }

    should("execute state updates specified in action block") {
      val commands = mutableListOf<BackStack>()
      val service = FlowNodeBuilder<Int, Unit>()
        .setInitial(NodeKey("a"))
        .addScreenNode(NodeKey("a")) { builder ->
          builder
            .on(Name("T")) {
              action {
                updateState { it + 8 }
                sendEvent(Event(Name("Y")))
              }
            }
            .build()
        }
        .addScreenNode(NodeKey("b")) { builder -> builder.build() }
        .addScreenNode(NodeKey("c")) { builder -> builder.build() }
        .on(Name("Y")) {
          if (state == 11) {
            navigateTo(NodeKey("c"))
          } else {
            navigateTo(NodeKey("b"))
          }
        }
        .build(initialState = 3)
        .unwrap()
        .toCollectingService(commands)

      service.sendEvent(Event(Name("T")))

      commands.last().shouldContainExactly(path("a"), path("c"))
    }
  }

  context("flow result processing") {
    should("transition to node specified in onFinish") {
      val commands = mutableListOf<BackStack>()
      val flowB = FlowNodeBuilder<Unit, String>()
          .setInitial(NodeKey("b1"))
          .addScreenNode(NodeKey("b1")) { sb -> sb.on(Name("T")) { finish("finishResultT") }.build() }
          .build(Unit)
          .unwrap()
      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowB"))
        .addFlowNode<String>(NodeKey("flowB")) { builder ->
          builder
            .of(flowB)
            .onFinish {
              if (result == "finishResultT") navigateTo(NodeKey("a1")) else error("unexpected result")
            }
            .build()
            .unwrap()
        }
        .addScreenNode(NodeKey("a1")) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      val service = node.toCollectingService(commands)

      service.sendEvent(Event(Name("T")))

      commands.last().shouldContainExactly(path("a1"))
    }

    should("properly finish parent flow in response to child onFinish") {
      val commands = mutableListOf<BackStack>()
      val flowC = FlowNodeBuilder<Unit, FlowResultY>()
          .setInitial(NodeKey("c1"))
          .addScreenNode(NodeKey("c1")) { sb -> sb.on(Name("T")) { finish(FlowResultY.Y2) }.build() }
          .build(Unit)
          .unwrap()

      val flowB = FlowNodeBuilder<Unit, FlowResultX>()
        .setInitial(NodeKey("b1"))
        .addScreenNode(NodeKey("b1")) { sb -> sb.on(Name("T")) { navigateTo(NodeKey("flowC")) }.build() }
        .addFlowNode<FlowResultY>(NodeKey("flowC")) { builder ->
          builder.of(flowC)
            .onFinish {
              when (result) {
                FlowResultY.Y1 -> finish(FlowResultX.X1)
                FlowResultY.Y2 -> finish(FlowResultX.X3)
              }
            }
            .build()
            .unwrap()
        }
        .build(Unit)
        .unwrap()

      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowB"))
        .addFlowNode<FlowResultX>(NodeKey("flowB")) { builder ->
          builder
            .of(flowB)
            .onFinish {
              if (result == FlowResultX.X3) navigateTo(NodeKey("a1"))
            }
            .build()
            .unwrap()
        }
        .addScreenNode(NodeKey("a1")) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      val service = node.toCollectingService(commands)

      service.sendEvent(Event(Name("T"))) // flowB.b1 → flowC
      service.sendEvent(Event(Name("T"))) // flowC → finish flowC

      commands.last().shouldContainExactly(path("a1"))
    }

    should("not pass final states to the command builder") {
      val commands = mutableListOf<BackStack>()
      val flowB = FlowNodeBuilder<Unit, String>()
        .setInitial(NodeKey("b1"))
        .addScreenNode(NodeKey("b1")) { sb -> sb.on(Name("T")) { finish("finishResultT") }.build() }
        .build(Unit)
        .unwrap()
      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowB"))
        .addFlowNode<String>(NodeKey("flowB")) { builder ->
          builder
            .of(flowB)
            .onFinish {
              if (result == "finishResultT") navigateTo(NodeKey("a1")) else error("unexpected result")
            }
            .build()
            .unwrap()
        }
        .addScreenNode(NodeKey("a1")) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      val service = node.toCollectingService(commands)

      service.sendEvent(Event(Name("T")))

      commands.forEach { backstack ->
        backstack.forEach { path -> path.asIterable() shouldNotContain FlowNode.DEFAULT_FINAL_NODE_KEY }
      }
    }

    should("not pass empty backstack the command builder when there are only final states in it") {
      val prevCommands = mutableListOf<BackStack>()
      val commands = mutableListOf<BackStack>()
      // having cascading "double-finish" here caused some bugs with backstack "purification" from done-states...
      // i.e when inner flowB finishes and outer flowA reacts to this with finish too, then calculation went off...
      val flowB = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("b1"))
        .addScreenNode(NodeKey("b1")) { sb -> sb.on(Name("T")) { finish(Unit) }.build() }
        .build(Unit)
        .unwrap()
      val flowA = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowB"))
        .addFlowNode<Unit>(NodeKey("flowB")) { builder ->
          builder.of(flowB)
            .onFinish { finish(Unit) }
            .build()
            .unwrap()
        }
        .addScreenNode(NodeKey("x")) { builder -> builder.build() }
        .build(Unit)
        .unwrap()
      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowA"))
        .addFlowNode<Unit>(NodeKey("flowA")) { builder ->
          builder
            .of(flowA)
            .onFinish {
              navigateTo(NodeKey("x"))
            }
            .build()
            .unwrap()
        }
        .addScreenNode(NodeKey("x")) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      val service = node.toService(
        commandBuilder = { old, new, _ -> prevCommands.add(old); commands.add(new); new },
        onCommand = { }
      )
      service.start()

      service.sendEvent(Event(Name("T")))

      commands.shouldContainExactly(
        listOf(path("flowA.flowB.b1")), // initial
        listOf(path("flowA.flowB.b1")), // after first done
        listOf(path("x")), // after second done
      )
      prevCommands.shouldContainExactly(
        emptyList(),
        listOf(path("flowA.flowB.b1")),
        listOf(path("flowA.flowB.b1")),
      )
    }

    should("process events sent from finish block in absence of navigate actions") {
      val commands = mutableListOf<BackStack>()
      val flowB = FlowNodeBuilder<Unit, String>()
        .setInitial(NodeKey("b1"))
        .addScreenNode(NodeKey("b1")) { sb -> sb.on(Name("T")) { finish("finishResultT") }.build() }
        .build(Unit)
        .unwrap()

      val node = FlowNodeBuilder<Unit, Unit>()
        .setInitial(NodeKey("flowB"))
        .addFlowNode<String>(NodeKey("flowB")) { builder ->
          builder
            .of(flowB)
            .onFinish {
              action {
                if (result == "finishResultT") sendEvent(Event(Name("TT"))) else error("unexpected result")
              }
            }
            .build()
            .unwrap()
        }
        .addScreenNode(NodeKey("a1")) { builder -> builder.build() }
        .on(Name("TT")) { navigateTo(NodeKey("a1")) }
        .build(Unit)
        .unwrap()

      val service = node.toCollectingService(commands)

      // will finish flowB which in turn will emit "TT" event to switch to "a1"
      service.sendEvent(Event(Name("T")))
      commands.last().shouldContainExactly(path("a1"))
    }
  }

  context("flow state management") {
    should("update state in node actions") {
      var updatedState: Any? = null
      val service = FlowNodeBuilder<List<String>, Unit>()
        .setInitial(NodeKey("a"))
        .onEntry { updateState { it.plus("flowNode_onEntry") } }
        .onExit { updateState { it.plus("flowNode_onExit") } }
        .addScreenNode(NodeKey("a")) { builder ->
          builder
            .onEntry { updateState { it.plus("screenNode_a_onEntry") } }
            .onExit { updateState { it.plus("screenNode_a_onExit") } }
            .on(Name("EN")) { navigateTo(NodeKey("b")) }
            .build()
        }
        .addScreenNode(NodeKey("b")) { builder ->
          builder
            .onEntry { updateState { it.plus("screenNode_b_onEntry") } }
            .onExit { updateState { it.plus("screenNode_b_onExit") } }
            .on(Name("EN")) { navigateTo(NodeKey("a")) }
            .build()
        }
        .build(emptyList())
        .unwrap()
        .toService(
          commandBuilder = { _, _, s -> updatedState = s },
          onCommand = { }
        ).apply {
          start()
        }

      service.sendEvent(Event(Name("EN"))) // a -> b
      service.sendEvent(Event(Name("EN"))) // b -> a

      (updatedState as List<String>).shouldContainInOrder(
        "flowNode_onEntry",
        "screenNode_a_onEntry",
        "screenNode_a_onExit",
        "screenNode_b_onEntry",
        "screenNode_b_onExit",
        "screenNode_a_onEntry",
      )
    }

    should("update independent state in sub flows") {
      TODO()
    }

    should("invoke command builder if backstack is the same but state changed") {
      var updatedState: Any? = null
      val service = FlowNodeBuilder<List<String>, Unit>()
        .setInitial(NodeKey("a"))
        .addScreenNode(NodeKey("a")) { builder ->
          builder
            .on(Name("EN")) {
              action {
                updateState { it.plus("a_event") }
              }
            }
            .build()
        }
        .build(emptyList())
        .unwrap()
        .toService(
          commandBuilder = { _, _, s -> updatedState = s },
          onCommand = { }
        ).apply {
          start()
        }

      service.sendEvent(Event(Name("EN")))
      service.sendEvent(Event(Name("EN")))

      (updatedState as List<String>) shouldContainExactly listOf("a_event", "a_event")
    }
  }
})

private fun NodeScheme.toCollectingService(
  commandSink: MutableList<BackStack>,
  start: Boolean = true,
): NavigationService<BackStack> {
  return this.toService(
    initialState = Unit,
    commandBuilder = { _, new, _ -> new },
    onCommand = { commandSink.add(it) }
  ).apply { if (start) start() }
}

private fun FlowNode<*, *>.toCollectingService(
  commandSink: MutableList<BackStack>,
  start: Boolean = true,
): NavigationService<BackStack> {
  return this.toService(
    commandBuilder = { _, new, _ -> new },
    onCommand = { commandSink.add(it) }
  ).apply { if (start) start() }
}

enum class FlowResultX { X1, X2, X3 }
enum class FlowResultY { Y1, Y2 }
