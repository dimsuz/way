package ru.dimsuz.way

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
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
      ).toCollectingService(mutableListOf())

      commands.shouldContainExactly(Replace(listOf(entry("a"))))
    }

    should("emit push for transition to the new screen") {
      val commands = mutableListOf<BackStackCommand>()
      val service = scheme(
        initial = "a",
        node("a", on("T", target = "b")),
        node("b", on("U", target = "c")),
        node("c")
      ).toCollectingService(mutableListOf())

      service.sendEvent(Event("T"))

      commands.shouldContainExactly(
        Replace(listOf(entry("a"))),
        Push(entry("b")),
        Push(entry("c")),
      )
    }
  }
})

private fun NodeScheme.toCollectingService(
  commandSink: MutableList<BackStackCommand>
): NavigationService {
  return this.toService(Unit, onCommand = { commandSink.add(it) })
}

private fun entry(key: String): BackStackEntry {
  return BackStackEntry(NodeKey(key))
}
