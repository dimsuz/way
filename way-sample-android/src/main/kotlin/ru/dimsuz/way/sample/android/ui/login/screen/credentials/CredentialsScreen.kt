package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.Screen

class CredentialsScreen : Screen {
  companion object {
    val key = "credentials"
  }

  private val viewModel = CredentialsViewModel()

  @Composable
  override fun Content() {
    CredentialsUi(viewModel = viewModel)
  }

  override fun onAttach() {
    viewModel.onAttach()
  }

  override fun onDetach() {
    viewModel.onDetach()
  }

  override fun destroy() {
    viewModel.destroy()
  }
}
