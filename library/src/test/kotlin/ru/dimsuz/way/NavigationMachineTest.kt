package ru.dimsuz.way

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import ru.dimsuz.way.generator.node

class NavigationMachineTest : ShouldSpec({
  context("initial state") {
    should("switch to initial state") {
      val screen = Arb.node().next()
      val flow = FlowNodeBuilder<Unit, Unit, Unit>()
        .setInitial(screen)
        .addScreenNode(screen) { builder ->  builder.build() }
        .build(Unit)

      val machine = NavigationMachine(flow)

      machine.initialNode shouldBe screen
    }
  }
})
