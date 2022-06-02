package ru.dimsuz.way.sample.android.ui.login.screen.otp

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseUi

@Composable
fun OtpUi(viewModel: OtpViewModel) {
  BaseUi(viewModel = viewModel) { state ->
    Text(state.title)
  }
}
