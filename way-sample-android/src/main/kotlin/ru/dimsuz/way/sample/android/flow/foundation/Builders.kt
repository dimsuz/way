package ru.dimsuz.way.sample.android.flow.foundation

import arrow.optics.Lens
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.ScreenNode
import ru.dimsuz.way.ScreenNodeBuilder
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec

fun <S : Any, R : Any> FlowNodeBuilder<S, R>.addSampleScreenNode(
  spec: ScreenNodeSpec,
  screenNodeSpecsLens: Lens<S, Map<NodeKey, ScreenNodeSpec>>,
  buildAction: (ScreenNodeBuilder<S, R>) -> ScreenNode
): FlowNodeBuilder<S, R> {
  return this.addScreenNode(spec.key) { builder ->
    builder
      .onEntry {
        updateState {
          screenNodeSpecsLens.modify(it) { specs ->
            specs.plus(spec.key to spec)
          }
        }
      }
      .let(buildAction)
  }
}
