package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseUi

@Composable
fun CredentialsUi(viewModel: CredentialsViewModel) {
  BaseUi(viewModel = viewModel) { state ->
    Text(state.title)
  }
}
