package ru.dimsuz.way.sample.android.ui.profile.screen.capture

import androidx.compose.runtime.Composable
import ru.dimsuz.way.NodeKey
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen
import ru.dimsuz.way.sample.android.ui.foundation.ScreenNodeSpec
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsScreen
import ru.dimsuz.way.sample.android.ui.login.screen.credentials.CredentialsViewModel

class CapturePhotoScreen(viewModel: CapturePhotoViewModel) : BaseScreen<CapturePhotoViewState, CapturePhotoViewModel>(viewModel) {
  companion object {
    val nodeSpec = ScreenNodeSpec(
      key = NodeKey("capture"),
      factory = { eventSink -> CapturePhotoScreen(CapturePhotoViewModel(eventSink)) }
    )
  }

  @Composable
  override fun Content(viewModel: CapturePhotoViewModel) {
    CapturePhotoUi(viewModel)
  }
}
