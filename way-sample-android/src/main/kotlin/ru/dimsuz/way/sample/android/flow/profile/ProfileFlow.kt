package ru.dimsuz.way.sample.android.flow.profile

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.event.AppStateEvent
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.permissions.PermissionsFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.profile.FlowEvent
import ru.dimsuz.way.sample.android.ui.profile.screen.capture.CapturePhotoScreen
import ru.dimsuz.way.sample.android.ui.profile.screen.capture.CapturePhotoViewModel
import ru.dimsuz.way.sample.android.ui.profile.screen.main.ProfileMainScreen
import ru.dimsuz.way.sample.android.ui.profile.screen.main.ProfileMainViewModel

object ProfileFlow {
  val key = NodeKey("profile_flow")

  data class State(
    override val screens: Map<NodeKey, Screen> = emptyMap(),
  ) : FlowState

  fun buildNode(eventSink: FlowEventSink): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey(ProfileMainScreen.key))
      .addScreenNode(NodeKey(ProfileMainScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("ProfileFlow", "onEntry Profile")
            val viewModel = ProfileMainViewModel(eventSink)
            updateState {
              it.copy(
                screens = state.screens.plus(
                  NodeKey(ProfileMainScreen.key) to ProfileMainScreen(
                    viewModel
                  )
                ),
              )
            }
          }
          .onExit {
            Log.d("ProfileFlow", "onExit Profile")
            updateState {
              it.copy(
                screens = state.screens.minus(NodeKey(ProfileMainScreen.key)),
              )
            }
          }
          .on(FlowEvent.CapturePhoto.name) {
            navigateTo(PermissionsFlow.key)
          }
          .build()
      }
      .addScreenNode(NodeKey(CapturePhotoScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("ProfileFlow", "onEntry Capture Photo")
            val viewModel = CapturePhotoViewModel(eventSink)
            updateState {
              it.copy(
                screens = state.screens.plus(
                  NodeKey(CapturePhotoScreen.key) to CapturePhotoScreen(
                    viewModel
                  )
                ),
              )
            }
          }
          .onExit {
            Log.d("ProfileFlow", "onExit Profile")
            updateState {
              it.copy(
                screens = state.screens.minus(NodeKey(CapturePhotoScreen.key)),
              )
            }
          }
          .on(Event.Name.BACK) {
            navigateTo(NodeKey(ProfileMainScreen.key))
          }
          .build()
      }
      .addFlowNode<PermissionsFlow.Result>(PermissionsFlow.key) { builder ->
        builder.of(PermissionsFlow.buildNode(eventSink))
          .onResult {
            when (result) {
              PermissionsFlow.Result.Granted -> navigateTo(NodeKey(CapturePhotoScreen.key))
              PermissionsFlow.Result.Denied -> sendEvent(AppStateEvent.PermissionsDeniedPermanently)
              PermissionsFlow.Result.Dismissed -> navigateTo(NodeKey(ProfileMainScreen.key))
            }
          }
          .build()
          .unwrap()
      }
      .build(State())
      .unwrap()
  }
}
