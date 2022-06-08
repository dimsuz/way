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
    val screens: Map<NodeKey, Screen>
  )

  fun buildNode(): FlowNode<State, Unit, FlowResult> {
    return FlowNodeBuilder<State, Unit, FlowResult>()
      .setInitial(NodeKey(CredentialsScreen.key))
      .addScreenNode(NodeKey(CredentialsScreen.key)) { builder ->
        builder
          .onEntry {
            Log.d("LoginFlow", "onEntry credentials")
            // TODO use update state!
            val viewModel = CredentialsViewModel { sendEvent(it) }
            updateState {
              state.copy(
                screens = state.screens.plus(
                  NodeKey(CredentialsScreen.key) to CredentialsScreen(
                    viewModel
                  )
                )
              )
            }
          }
          .onExit {
            Log.d("LoginFlow", "onExit credentials")
            updateState {
              state.copy(
                screens = state.screens.minus(NodeKey(CredentialsScreen.key))
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
            Log.d("LoginFlow", "onEntry otp")
            // TODO use update state!
            val viewModel = OtpViewModel { sendEvent(it) }
            updateState {
              state.copy(
                screens = state.screens.plus(
                  NodeKey(OtpScreen.key) to OtpScreen(
                    viewModel
                  )
                )
              )
            }
          }
          .onExit {
            Log.d("LoginFlow", "onExit otp")
            updateState {
              state.copy(
                screens = state.screens.minus(NodeKey(OtpScreen.key))
              )
            }
          }
          .on(FlowEvent.Continue.name) {
            finish(FlowResult.Success)
          }
          .on(Event.Name.BACK) {
            navigateTo(NodeKey(CredentialsScreen.key))
          }
          .build()
      }
      .build(Unit)
      .unwrap()
  }
}
