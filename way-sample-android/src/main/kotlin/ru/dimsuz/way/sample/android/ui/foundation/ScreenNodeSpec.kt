package ru.dimsuz.way.sample.android.ui.foundation

import ru.dimsuz.way.NodeKey

data class ScreenNodeSpec(
  val key: NodeKey,
  val factory: (FlowEventSink) -> Screen
)
