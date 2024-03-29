package ru.dimsuz.way.sample.android.flow.app

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.event.AppStateEvent
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.login.LoginFlow
import ru.dimsuz.way.sample.android.flow.main.MainFlow
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec

object AppFlow {
  data class State(
    override val screenNodeSpecs: Map<NodeKey, ScreenNodeSpec> = emptyMap(),
    val permissionsGranted: Boolean = false,
  ) : FlowState

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(LoginFlow.key)
      .addFlowNode<FlowResult>(LoginFlow.key) { builder ->
        builder.of(LoginFlow.buildNode())
          .onFinish {
            when (result) {
              FlowResult.Success -> navigateTo(MainFlow.key)
              FlowResult.Dismissed -> {
                Log.d("AppFlow", "finishing app flow")
                // TODO stack overflow if uncomment this: finish(result)
              }
            }
          }
          .build()
          .unwrap()
      }
      .addFlowNode<FlowResult>(MainFlow.key) { builder ->
        builder.of(MainFlow.buildNode())
          .onFinish {
            Log.d("AppFlow", "finishing app flow")
            // TODO stack overflow if uncomment this: finish(result)
          }
          .build()
          .unwrap()
      }
      .on(Event.Name.BACK) {
        finish(FlowResult.Dismissed)
      }
      .on(AppStateEvent.PermissionsDeniedPermanently.name) {
        navigateTo(LoginFlow.key)
      }
      .on(AppStateEvent.AuthTokensExpired.name) {
        navigateTo(LoginFlow.key)
      }
      .build(State())
      .unwrap()
  }
}
