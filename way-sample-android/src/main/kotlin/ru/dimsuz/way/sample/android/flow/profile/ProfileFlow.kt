package ru.dimsuz.way.sample.android.flow.profile

import android.util.Log
import arrow.optics.optics
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.event.AppStateEvent
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.addSampleScreenNode
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.permissions.PermissionsFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.profile.FlowEvent
import ru.dimsuz.way.sample.android.ui.profile.screen.capture.CapturePhotoScreen
import ru.dimsuz.way.sample.android.ui.profile.screen.capture.CapturePhotoViewModel
import ru.dimsuz.way.sample.android.ui.profile.screen.main.ProfileMainScreen
import ru.dimsuz.way.sample.android.ui.profile.screen.main.ProfileMainViewModel

object ProfileFlow {
  val key = NodeKey("profile_flow")

  @optics
  data class State(
    override val screenNodeSpecs: Map<NodeKey, ScreenNodeSpec> = emptyMap(),
  ) : FlowState {
    companion object
  }

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(ProfileMainScreen.nodeSpec.key)
      .addSampleScreenNode(ProfileMainScreen.nodeSpec, State.screenNodeSpecs) { builder ->
        builder
          .on(FlowEvent.CapturePhoto.name) {
            navigateTo(PermissionsFlow.key)
          }
          .build()
      }
      .addSampleScreenNode(CapturePhotoScreen.nodeSpec, State.screenNodeSpecs) { builder ->
        builder
          .on(Event.Name.BACK) {
            navigateTo(ProfileMainScreen.nodeSpec.key)
          }
          .build()
      }
      .addFlowNode<PermissionsFlow.Result>(PermissionsFlow.key) { builder ->
        builder.of(PermissionsFlow.buildNode())
          .onFinish {
            when (result) {
              PermissionsFlow.Result.Granted -> navigateTo(CapturePhotoScreen.nodeSpec.key)
              PermissionsFlow.Result.Denied -> sendEvent(AppStateEvent.PermissionsDeniedPermanently)
              PermissionsFlow.Result.Dismissed -> navigateTo(ProfileMainScreen.nodeSpec.key)
            }
          }
          .build()
          .unwrap()
      }
      .build(State())
      .unwrap()
  }
}
