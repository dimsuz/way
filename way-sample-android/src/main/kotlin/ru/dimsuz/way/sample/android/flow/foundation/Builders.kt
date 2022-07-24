package ru.dimsuz.way.sample.android.flow.foundation

import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.ScreenNode
import ru.dimsuz.way.ScreenNodeBuilder
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec

fun <S : FlowState, A : Any, R : Any> FlowNodeBuilder<S, A, R>.addSampleScreenNode(
  spec: ScreenNodeSpec,
  eventSink: FlowEventSink,
  buildAction: (ScreenNodeBuilder<S, A, R>) -> ScreenNode
): FlowNodeBuilder<S, A, R> {
  return this.addScreenNode(spec.key) { builder ->
    builder
      .onEntry {
//        updateState {
//          it.copy(
//            screens = state.screens.plus(
//              NodeKey(CredentialsScreen.key) to CredentialsScreen(
//                viewModel
//              )
//            ),
//            logs = it.logs + "credentials onEntry"
//          )
//        }
      }
      .onExit {
//        updateState {
//          it.copy(
//            screens = state.screens.minus(NodeKey(CredentialsScreen.key)),
//            logs = it.logs + "credentials onExit"
//          )
//        }
      }
      .let(buildAction)
  }
}
