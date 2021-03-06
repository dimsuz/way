package ru.dimsuz.way.sample.android.ui.profile.screen.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dimsuz.way.sample.android.ui.foundation.BaseUi

@Composable
fun CapturePhotoUi(viewModel: CapturePhotoViewModel) {
  BaseUi(viewModel = viewModel) { state ->
    Column(
      modifier = Modifier.padding(24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(state.title, color = contentColorFor(backgroundColor = state.color), modifier = Modifier.background(state.color).padding(16.dp))
      Button(onClick = viewModel::onContinue) {
        Text("Continue")
      }
      Button(onClick = viewModel::onBack) {
        Text("Back")
      }
    }
  }
}
