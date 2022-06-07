package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.BaseUi
import ru.dimsuz.way.sample.android.ui.foundation.BaseViewModel
import ru.dimsuz.way.sample.android.ui.foundation.Screen

class CredentialsScreen(viewModel: CredentialsViewModel) : BaseScreen<CredentialsViewState, CredentialsViewModel>(viewModel) {
  companion object {
    val key = "credentials"
  }

  @Composable
  override fun Content(viewModel: CredentialsViewModel) {
    CredentialsUi(viewModel)
  }
}
