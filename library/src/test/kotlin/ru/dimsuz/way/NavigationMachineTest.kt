package ru.dimsuz.way

import com.github.michaelbull.result.getError
import com.github.michaelbull.result.unwrap
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import ru.dimsuz.way.generator.nodeId

class NavigationMachineTest : ShouldSpec({
  context("initial state") {
    should("report error if initial state is missing") {
      val screen = Arb.nodeId().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .addScreenNode(screen) { builder ->  builder.build() }
        .build(Unit)

      node.getError() shouldBe FlowNodeBuilder.Error.MissingInitialNode
    }

    should("switch to initial state") {
      val screen = Arb.nodeId().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .setInitial(screen)
        .addScreenNode(screen) { builder ->  builder.build() }
        .build(Unit)
        .unwrap()

      val machine = NavigationMachine(node)

      machine.initialNode shouldBe screen
    }
  }

  context("transitions") {
    should("perform simple transition") {
      val screen = Arb.nodeId().next()
      val node = FlowNodeBuilder<Unit, Unit, Unit>()
        .addScreenNode(screen) { builder ->  builder.build() }
        .build(Unit)

      node.getError() shouldBe FlowNodeBuilder.Error.MissingInitialNode
    }
  }
})
