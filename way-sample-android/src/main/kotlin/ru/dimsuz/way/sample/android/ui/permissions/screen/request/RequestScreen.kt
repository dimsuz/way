package ru.dimsuz.way.sample.android.ui.permissions.screen.request

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class RequestScreen(viewModel: RequestViewModel) : BaseScreen<RequestViewState, RequestViewModel>(viewModel) {
  companion object {
    val key = "request"
  }

  @Composable
  override fun Content(viewModel: RequestViewModel) {
    RequestUi(viewModel)
  }
}
