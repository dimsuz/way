package ru.dimsuz.way.sample.android.ui.main.screen.main

import androidx.compose.runtime.Composable
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel

class MainScreen(viewModel: MainViewModel) : BaseScreen<MainViewState, MainViewModel>(viewModel) {
  companion object {
    val nodeSpec = ScreenNodeSpec(
      key = NodeKey("main"),
      factory = { eventSink -> MainScreen(MainViewModel(eventSink)) }
    )
  }

  @Composable
  override fun Content(viewModel: MainViewModel) {
    MainUi(viewModel)
  }
}
