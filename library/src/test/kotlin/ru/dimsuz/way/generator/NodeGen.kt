package ru.dimsuz.way.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.stringPattern
import ru.dimsuz.way.NodeId
import ru.dimsuz.way.ScreenNode

fun Arb.Companion.nodeId(): Arb<NodeId> {
  return Arb.stringPattern("[A-Za-z]{5,8}").map { NodeId(it) }
}
