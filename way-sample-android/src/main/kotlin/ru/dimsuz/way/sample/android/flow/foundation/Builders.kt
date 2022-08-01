package ru.dimsuz.way.sample.android.flow.foundation

import arrow.optics.Lens
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.ScreenNode
import ru.dimsuz.way.ScreenNodeBuilder
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.login.LoginFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen

fun <S : Any, A : Any, R : Any> FlowNodeBuilder<S, A, R>.addSampleScreenNode(
  spec: ScreenNodeSpec,
  screenNodeSpecsLens: Lens<S, Map<NodeKey, ScreenNodeSpec>>,
  buildAction: (ScreenNodeBuilder<S, A, R>) -> ScreenNode
): FlowNodeBuilder<S, A, R> {
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
