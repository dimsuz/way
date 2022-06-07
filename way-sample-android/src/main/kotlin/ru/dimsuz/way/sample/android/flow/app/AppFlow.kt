package ru.dimsuz.way.sample.android.flow.app

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.login.LoginFlow

object AppFlow {
  data class State(
    val permissionsGranted: Boolean = false,
  )

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey("login"))
      .addFlowNode<FlowResult>(NodeKey("login")) { builder ->
        builder.of(LoginFlow.buildNode())
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
