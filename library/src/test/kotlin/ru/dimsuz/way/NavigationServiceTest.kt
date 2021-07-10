package ru.dimsuz.way

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import ru.dimsuz.way.entity.NodeScheme
import ru.dimsuz.way.entity.node
import ru.dimsuz.way.entity.on
import ru.dimsuz.way.entity.scheme
import ru.dimsuz.way.entity.toService

class NavigationServiceTest : ShouldSpec({
  context("unspecified") {
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

private fun path(s: String): Path {
  val segments = s.split(".").map { NodeKey(it) }
  return Path(segments.first(), segments.drop(1))
}
