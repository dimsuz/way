package ru.dimsuz.way.sample.android.flow.foundation.compose

import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec

interface FlowState {
  val screenNodeSpecs: Map<NodeKey, ScreenNodeSpec>
}
