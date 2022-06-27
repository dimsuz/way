package ru.dimsuz.way.sample.android.flow.foundation.compose

import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.Screen

interface FlowState {
  val screens: Map<NodeKey, Screen>
}
