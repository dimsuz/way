package ru.dimsuz.way.sample.android.ui.login.screen.otp

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class OtpScreen(viewModel: OtpViewModel) : BaseScreen<OtpViewState, OtpViewModel>(viewModel) {
  companion object {
    val key = "otp"
  }

  @Composable
  override fun Content(viewModel: OtpViewModel) {
    OtpUi(viewModel)
  }
}
