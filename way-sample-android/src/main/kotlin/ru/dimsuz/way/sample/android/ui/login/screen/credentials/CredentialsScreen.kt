package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class CredentialsScreen(viewModel: CredentialsViewModel) : BaseScreen<CredentialsViewState, CredentialsViewModel>(viewModel) {
  companion object {
    val key = "credentials"
  }

  @Composable
  override fun Content(viewModel: CredentialsViewModel) {
    CredentialsUi(viewModel)
  }
}
