package ru.dimsuz.way.sample.android.ui.main.screen.main

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class MainScreen(viewModel: MainViewModel) : BaseScreen<MainViewState, MainViewModel>(viewModel) {
  companion object {
    val key = "main"
  }

  @Composable
  override fun Content(viewModel: MainViewModel) {
    MainUi(viewModel)
  }
}
