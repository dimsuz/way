package ru.dimsuz.way.sample.android.ui.profile.screen.capture

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class CapturePhotoViewState(
  val title: String = "CapturePhoto",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
