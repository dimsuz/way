package ru.dimsuz.way.sample.android.flow.main

import arrow.optics.optics
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.addSampleScreenNode
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.profile.ProfileFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.main.FlowEvent
import ru.dimsuz.way.sample.android.ui.main.screen.main.MainScreen

object MainFlow {
  val key = NodeKey("main_flow")

  @optics
  data class State(
    override val screenNodeSpecs: Map<NodeKey, ScreenNodeSpec> = emptyMap(),
  ) : FlowState {
    companion object
  }

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(MainScreen.nodeSpec.key)
      .addSampleScreenNode(MainScreen.nodeSpec, State.screenNodeSpecs) { builder ->
        builder
          .on(FlowEvent.ViewProfile.name) {
            navigateTo(ProfileFlow.key)
          }
          .on(Event.Name.BACK) {
            finish(FlowResult.Dismissed)
          }
          .build()
      }
      .addFlowNode<FlowResult>(ProfileFlow.key) { builder ->
        builder.of(ProfileFlow.buildNode())
          .build()
          .unwrap()
      }
      .build(State())
      .unwrap()
  }
}
