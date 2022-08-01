package ru.dimsuz.way.sample.android.ui.profile.screen.main

import androidx.compose.runtime.Composable
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel

class ProfileMainScreen(viewModel: ProfileMainViewModel) : BaseScreen<ProfileMainViewState, ProfileMainViewModel>(viewModel) {
  companion object {
    val nodeSpec = ScreenNodeSpec(
      key = NodeKey("profile_main"),
      factory = { eventSink -> ProfileMainScreen(ProfileMainViewModel(eventSink)) }
    )
  }

  @Composable
  override fun Content(viewModel: ProfileMainViewModel) {
    ProfileMainUi(viewModel)
  }
}
