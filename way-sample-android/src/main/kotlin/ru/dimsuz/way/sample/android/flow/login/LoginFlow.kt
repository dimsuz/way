package ru.dimsuz.way.sample.android.flow.login

import com.github.michaelbull.result.unwrap
import ru.dimsuz.way.FlowNode
import ru.dimsuz.way.FlowNodeBuilder
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.flow.foundation.FlowResult
import ru.dimsuz.way.sample.android.ui.login.FlowEvent
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.otp.OtpScreen

object LoginFlow {
  fun buildNode(): FlowNode<Unit, Unit, FlowResult> {
    return FlowNodeBuilder<Unit, Unit, FlowResult>()
      .setInitial(NodeKey(CredentialsScreen.key))
      .addScreenNode(NodeKey(CredentialsScreen.key)) { builder ->
        builder
          .on(FlowEvent.Continue.name) {
            navigateTo(NodeKey(OtpScreen.key))
          }
          .build()
      }
      .addScreenNode(NodeKey(OtpScreen.key)) { builder ->
        builder
          .on(FlowEvent.Continue.name) {
            finish(FlowResult.Success)
          }
          .build()
      }
      .build(Unit)
      .unwrap()
  }
}
