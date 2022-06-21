package ru.dimsuz.way.sample.android.flow.login

import android.util.Log
import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.Event
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.ui.foundation.Screen
import ru.dimsuz.way.sample.android.ui.login.FlowEvent
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpScreen
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpViewModel

object LoginFlow {

  data class State(
    val screens: Map<NodeKey, Screen> = emptyMap(),
    val logs: List<String> = emptyList(),
  )

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey(CredentialsScreen.key))
      .addScreenNode(NodeKey(CredentialsScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("LoginFlow", "onEntry credentials, state is ${state.logs}")
            val viewModel = CredentialsViewModel { sendEvent(it) }
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
            updateState { it.copy(logs = it.logs + "credentials onContinue") }
            navigateTo(NodeKey(OtpScreen.key))
          }
          .build()
      }
      .addScreenNode(NodeKey(OtpScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("LoginFlow", "onEntry otp, state is ${state.logs}")
            // TODO use update state!
            val viewModel = OtpViewModel { sendEvent(it) }
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
          .on(FlowEvent.Continue.name) {
            finish(FlowResult.Success)
          }
          .on(Event.Name.BACK) {
            updateState { it.copy(logs = it.logs + "otp onContinue") }
            navigateTo(NodeKey(CredentialsScreen.key))
          }
          .build()
      }
      .build(State())
      .unwrap()
  }
}
