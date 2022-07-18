package ru.dimsuz.way.sample.android.flow.app

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.login.LoginFlow
import ru.dimsuz.way.sample.android.flow.main.MainFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen

object AppFlow {
  data class State(
    override val screens: Map<NodeKey, Screen> = emptyMap(),
    val permissionsGranted: Boolean = false,
  ) : FlowState

  fun buildNode(eventSink: FlowEventSink): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey("login"))
      .addFlowNode<FlowResult>(NodeKey("login")) { builder ->
        builder.of(LoginFlow.buildNode(eventSink))
          .onResult {
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
      .addFlowNode<FlowResult>(NodeKey("main")) { builder ->
        builder.of(MainFlow.buildNode(eventSink))
          .onResult {
            Log.d("AppFlow", "finishing app flow")
            // TODO stack overflow if uncomment this: finish(result)
          }
          .build()
          .unwrap()
      }
      .on(Event.Name.BACK) {
        finish(FlowResult.Dismissed)
      }
      .build(State())
      .unwrap()
  }
}
