package ru.dimsuz.way.sample.android.ui.permissions.screen.request

import androidx.compose.runtime.Composable
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel

class RequestScreen(viewModel: RequestViewModel) : BaseScreen<RequestViewState, RequestViewModel>(viewModel) {
  companion object {
    val nodeSpec = ScreenNodeSpec(
      key = NodeKey("permissions_request"),
      factory = { eventSink -> RequestScreen(RequestViewModel(eventSink)) }
    )
  }

  @Composable
  override fun Content(viewModel: RequestViewModel) {
    RequestUi(viewModel)
  }
}
