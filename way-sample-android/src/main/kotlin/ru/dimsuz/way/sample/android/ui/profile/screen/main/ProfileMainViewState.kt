package ru.dimsuz.way.sample.android.ui.profile.screen.main

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class ProfileMainViewState(
  val title: String = "ProfileMain",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
