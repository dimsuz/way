package ru.dimsuz.way.sample.android.flow.login

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.flow.foundation.addSampleScreenNode
import ru.dimsuz.way.sample.android.flow.foundation.compose.FlowState
import ru.dimsuz.way.sample.android.flow.permissions.PermissionsFlow
import ru.dimsuz.way.sample.android.ui.foundation.FlowEventSink
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.login.FlowEvent
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpScreen
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpViewModel

object LoginFlow {
  val key = NodeKey("login")

  data class State(
    override val screens: Map<NodeKey, Screen> = emptyMap(),
    val logs: List<String> = emptyList(),
  ) : FlowState

  fun buildNode(eventSink: FlowEventSink): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(CredentialsScreen.nodeSpec.key)
      .addSampleScreenNode(CredentialsScreen.nodeSpec, eventSink) { builder ->
        builder
          .on(FlowEvent.Continue.name) {
            navigateTo(NodeKey(OtpScreen.key))
          }
          .build()
      }
      .addScreenNode(NodeKey(OtpScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("LoginFlow", "onEntry otp, state is ${state.logs}")
            val viewModel = OtpViewModel(eventSink)
            updateState {
              it.copy(
                screens = state.screens.plus(
                  NodeKey(OtpScreen.key) to OtpScreen(
                    viewModel
                  )
                ),
                logs = it.logs + "otp onEntry"
              )
            }
          }
          .onExit {
            Log.d("LoginFlow", "onExit otp, state is ${state.logs}")
            updateState {
              it.copy(
                screens = state.screens.minus(NodeKey(OtpScreen.key)),
                logs = it.logs + "otp onExit"
              )
            }
          }
          .on(FlowEvent.OtpSuccess.name) {
            navigateTo(PermissionsFlow.key)
          }
          .on(FlowEvent.OtpError.name) {
            Log.d("LoginFlow", "TODO handle otp error")
          }
          .on(Event.Name.BACK) {
            navigateTo(CredentialsScreen.nodeSpec.key)
          }
          .build()
      }
      .addFlowNode<PermissionsFlow.Result>(PermissionsFlow.key) { builder ->
        builder.of(PermissionsFlow.buildNode(eventSink))
          .onResult {
            when (result) {
              PermissionsFlow.Result.Granted -> finish(FlowResult.Success)
              PermissionsFlow.Result.Denied -> navigateTo(CredentialsScreen.nodeSpec.key)
              PermissionsFlow.Result.Dismissed -> navigateTo(CredentialsScreen.nodeSpec.key)
            }
          }
          .build()
          .unwrap()
      }
      .build(State())
      .unwrap()
  }
}
