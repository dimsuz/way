package ru.dimsuz.way.generator

import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.stringPattern
import ru.dimsuz.way.Event
import ru.dimsuz.way.NodeKey

fun Arb.Companion.nodeKey(): Arb<NodeKey> {
  return Arb.stringPattern("[a-z]{5,8}").map { NodeKey(it) }
}

fun Arb.Companion.event(): Arb<Event> {
  return Arb.stringPattern("[A-Z]{5,8}").map { Event(it) }
}
