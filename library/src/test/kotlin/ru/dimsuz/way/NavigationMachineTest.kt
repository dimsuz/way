package ru.dimsuz.way

import com.github.michaelbull.result.getError
import com.github.michaelbull.result.unwrap
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import ru.dimsuz.way.entity.isValidTransition
import ru.dimsuz.way.generator.leafFlowNodeScheme
import ru.dimsuz.way.generator.nodeId
import ru.dimsuz.way.generator.screenEvents

class NavigationMachineTest : ShouldSpec({
  context("initial state") {
    should("report error if initial state is missing") {
      val screen = Arb.nodeId().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .addScreenNode(screen) { builder -> builder.build() }
        .build(Unit)

      node.getError() shouldBe FlowNodeBuilder.Error.MissingInitialNode
    }

    should("switch to initial state") {
      val screen = Arb.nodeId().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .setInitial(screen)
        .addScreenNode(screen) { builder -> builder.build() }
        .build(Unit)
        .unwrap()

      val machine = NavigationMachine<Unit, Unit, Unit>(node)

      machine.initialNodeId shouldBe screen
    }
  }

  context("transitions") {
    should("perform simple transitions") {
      checkAll(
        Arb.leafFlowNodeScheme().flatMap { scheme -> Arb.screenEvents(scheme).map { scheme to it } }
      ) { (scheme, screenEvents) ->
        val node = FlowNodeBuilder<Unit, Unit, Unit>()
          .setInitial(scheme.states.keys.first())
          .apply {
            scheme.states.entries.forEach { (screenId, transitions) ->
              addScreenNode(screenId) { builder ->
                transitions.forEach { (event, target) ->
                  builder.on(event) { navigateTo(target) }
                }
                builder.build()
              }
            }
          }
          .build(Unit)
          .unwrap()
        val machine = NavigationMachine(node)

        machine.runTransitionSequence(
          { screenEvents[it] },
          onTransition = { prev, event, next ->
            withClue("prev = $prev, event = $event, next = $next") {
              scheme.isValidTransition(prev, event, next).shouldBeTrue()
            }
          }
        )
      }
    }
  }
})

private fun <S : Any, A : Any, R : Any> NavigationMachine<S, A, R>.runTransitionSequence(
  nextEventSelector: (node: NodeId) -> Event?,
  onTransition: (prev: NodeId, event: Event, next: NodeId) -> Unit
): NodeId {
  var currentNode = this.initialNodeId
  while (true) {
    val nextEvent = nextEventSelector(currentNode) ?: return currentNode
    val prev = currentNode
    currentNode = this.transition(currentNode, nextEvent)
    onTransition(prev, nextEvent, currentNode)
  }
}
