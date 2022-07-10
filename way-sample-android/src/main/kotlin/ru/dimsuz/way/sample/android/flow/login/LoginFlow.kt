package ru.dimsuz.way.sample.android.flow.login

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
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

  data class State(
    override val screens: Map<NodeKey, Screen> = emptyMap(),
    val logs: List<String> = emptyList(),
  ) : FlowState

  fun buildNode(eventSink: FlowEventSink): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey(CredentialsScreen.key))
      .addScreenNode(NodeKey(CredentialsScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("LoginFlow", "onEntry credentials, state is ${state.logs}")
            val viewModel = CredentialsViewModel(eventSink)
            updateState {
              it.copy(
                screens = state.screens.plus(
                  NodeKey(CredentialsScreen.key) to CredentialsScreen(
                    viewModel
                  )
                ),
                logs = it.logs + "credentials onEntry"
              )
            }
          }
          .onExit {
            Log.d("LoginFlow", "onExit credentials, state is ${state.logs}")
            updateState {
              it.copy(
                screens = state.screens.minus(NodeKey(CredentialsScreen.key)),
                logs = it.logs + "credentials onExit"
              )
            }
          }
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
            navigateTo(NodeKey(CredentialsScreen.key))
          }
          .build()
      }
      .addFlowNode<FlowResult>(PermissionsFlow.key) { builder ->
        builder.of(PermissionsFlow.buildNode(eventSink))
          .build()
          .unwrap()
      }
      .build(State())
      .unwrap()
  }
}
