package ru.dimsuz.way.sample.android.ui.permissions.screen.request

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class RequestViewState(
  val title: String = "Permissions Request",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
