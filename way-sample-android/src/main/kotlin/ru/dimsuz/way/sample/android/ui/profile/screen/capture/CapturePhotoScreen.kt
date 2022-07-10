package ru.dimsuz.way.sample.android.ui.profile.screen.capture

import androidx.compose.runtime.Composable
import ru.dimsuz.way.sample.android.ui.foundation.BaseScreen

class CapturePhotoScreen(viewModel: CapturePhotoViewModel) : BaseScreen<CapturePhotoViewState, CapturePhotoViewModel>(viewModel) {
  companion object {
    val key = "capture"
  }

  @Composable
  override fun Content(viewModel: CapturePhotoViewModel) {
    CapturePhotoUi(viewModel)
  }
}
