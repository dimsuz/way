package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import androidx.compose.runtime.Composable
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec

class CredentialsScreen(viewModel: CredentialsViewModel) : BaseScreen<CredentialsViewState, CredentialsViewModel>(viewModel) {
  companion object {
    val nodeSpec = ScreenNodeSpec(
      key = NodeKey("credentials"),
      factory = { eventSink -> CredentialsScreen(CredentialsViewModel(eventSink)) }
    )
  }

  @Composable
  override fun Content(viewModel: CredentialsViewModel) {
    CredentialsUi(viewModel)
  }
}
