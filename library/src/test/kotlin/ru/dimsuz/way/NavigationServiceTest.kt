package ru.dimsuz.way

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import ru.dimsuz.way.BackStackCommand.Pop
import ru.dimsuz.way.BackStackCommand.Push
import ru.dimsuz.way.BackStackCommand.Replace
import ru.dimsuz.way.entity.NodeScheme
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.scheme
import ru.dimsuz.way.entity.toService

class NavigationServiceTest : ShouldSpec({
  context("unspecified") {
    should("push initial route") {
      val commands = mutableListOf<BackStackCommand>()
      scheme(
        initial = "a",
        node("a")
      ).toCollectingService(commands)

      commands.shouldContainExactly(Replace(listOf(entry("a"))))
    }

    should("emit nothing if staying on the same node after ignoring event") {
      val commands = mutableListOf<BackStackCommand>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b"),
      ).toCollectingService(commands)

      service.sendEvent(Event("UNKNOWN"))

      commands.shouldContainExactly(
        Replace(listOf(entry("a"))),
      )
    }

    should("emit nothing if staying on the same node after transition to the same state") {
      val commands = mutableListOf<BackStackCommand>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "a")),
        node("b"),
      ).toCollectingService(commands)

      service.sendEvent(Event("T"))

      commands.shouldContainExactly(
        Replace(listOf(entry("a"))),
      )
    }

    should("emit push for transition to the new screen") {
      val commands = mutableListOf<BackStackCommand>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b", on("U", target = "c")),
        node("c")
      ).toCollectingService(commands)

      service.sendEvent(Event("T"))
      service.sendEvent(Event("U"))

      commands.shouldContainExactly(
        Replace(listOf(entry("a"))),
        Push(entry("b")),
        Push(entry("c")),
      )
    }

    should("emit pop for transition to the previous screen") {
      val commands = mutableListOf<BackStackCommand>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b", on("U", target = "c")),
        node("c", on("BACK", target = "a")),
      ).toCollectingService(commands)

      service.sendEvent(Event("T"))
      service.sendEvent(Event("U"))
      service.sendEvent(Event("BACK"))

      commands.takeLast(3).shouldContainExactly(
        Push(entry("b")),
        Push(entry("c")),
        Pop(count = 2),
      )
    }
  }
})

private fun NodeScheme.toCollectingService(
  commandSink: MutableList<BackStackCommand>,
  start: Boolean = true,
): NavigationService {
  return this.toService(Unit, onCommand = { commandSink.add(it) }).apply { if (start) start() }
}

private fun entry(key: String): BackStackEntry {
  return BackStackEntry(NodeKey(key))
}
