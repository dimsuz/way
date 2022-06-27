package ru.dimsuz.way.sample.android.ui.login.screen.otp

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class OtpViewState(
  val title: String = "Otp",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
