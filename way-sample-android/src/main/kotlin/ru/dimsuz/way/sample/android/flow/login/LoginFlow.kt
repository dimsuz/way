package ru.dimsuz.way.sample.android.flow.login

import android.util.Log
import arrow.optics.optics
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
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.FlowEvent
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpScreen
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpViewModel

object LoginFlow {
  val key = NodeKey("login")

  @optics
  data class State(
    override val screenNodeSpecs: Map<NodeKey, ScreenNodeSpec> = emptyMap(),
    val logs: List<String> = emptyList(),
  ) : FlowState {
    companion object
  }

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(CredentialsScreen.nodeSpec.key)
      .addSampleScreenNode(CredentialsScreen.nodeSpec, State.screenNodeSpecs) { builder ->
        builder
          .on(FlowEvent.Continue.name) {
            navigateTo(OtpScreen.nodeSpec.key)
          }
          .build()
      }
      .addSampleScreenNode(OtpScreen.nodeSpec, State.screenNodeSpecs) { builder ->
        builder
          .on(FlowEvent.Continue.name) {
            navigateTo(OtpScreen.nodeSpec.key)
          }
          .on(FlowEvent.OtpSuccess.name) {
            navigateTo(PermissionsFlow.key)
          }
          .on(FlowEvent.OtpError.name) {
            navigateTo(CredentialsScreen.nodeSpec.key)
          }
          .on(Event.Name.BACK) {
            navigateTo(CredentialsScreen.nodeSpec.key)
          }
          .build()
      }
      .addFlowNode<PermissionsFlow.Result>(PermissionsFlow.key) { builder ->
        builder.of(PermissionsFlow.buildNode())
          .onFinish {
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
