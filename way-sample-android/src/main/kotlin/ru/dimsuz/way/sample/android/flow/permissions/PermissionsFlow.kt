package ru.dimsuz.way.sample.android.flow.permissions

import android.util.Log
import arrow.optics.optics
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.addSampleScreenNode
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.permissions.FlowEvent
import ru.dimsuz.way.sample.android.ui.permissions.screen.request.RequestScreen
import ru.dimsuz.way.sample.android.ui.permissions.screen.request.RequestViewModel

object PermissionsFlow {
  val key = NodeKey("permissions_flow")

  @optics
  data class State(
    override val screenNodeSpecs: Map<NodeKey, ScreenNodeSpec> = emptyMap(),
  ) : FlowState {
    companion object
  }

  enum class Result {
    Granted,
    Denied,
    Dismissed
  }

  fun buildNode(): FlowNode<State, Unit, Result> {
    return FlowNodeBuilder<State, Unit, Result>()
      .setInitial(RequestScreen.nodeSpec.key)
      .addSampleScreenNode(RequestScreen.nodeSpec, State.screenNodeSpecs) { builder ->
        builder
          .on(FlowEvent.Granted.name) {
            finish(Result.Granted)
          }
          .on(FlowEvent.Denied.name) {
            finish(Result.Denied)
          }
          .on(Event.Name.BACK) {
            finish(Result.Dismissed)
          }
          .build()
      }
      .build(State())
      .unwrap()
  }
}
