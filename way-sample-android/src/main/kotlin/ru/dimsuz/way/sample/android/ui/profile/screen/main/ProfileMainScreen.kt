package ru.dimsuz.way.sample.android.ui.profile.screen.main

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class ProfileMainScreen(viewModel: ProfileMainViewModel) : BaseScreen<ProfileMainViewState, ProfileMainViewModel>(viewModel) {
  companion object {
    val key = "main"
  }

  @Composable
  override fun Content(viewModel: ProfileMainViewModel) {
    ProfileMainUi(viewModel)
  }
}
