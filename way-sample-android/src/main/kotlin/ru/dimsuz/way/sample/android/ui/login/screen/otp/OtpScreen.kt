package ru.dimsuz.way.sample.android.ui.login.screen.otp

import androidx.compose.runtime.Composable
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel

class OtpScreen(viewModel: OtpViewModel) : BaseScreen<OtpViewState, OtpViewModel>(viewModel) {
  companion object {
    val nodeSpec = ScreenNodeSpec(
      key = NodeKey("otp"),
      factory = { eventSink -> OtpScreen(OtpViewModel(eventSink)) }
    )
  }

  @Composable
  override fun Content(viewModel: OtpViewModel) {
    OtpUi(viewModel)
  }
}
