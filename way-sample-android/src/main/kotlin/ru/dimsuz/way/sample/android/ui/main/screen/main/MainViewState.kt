package ru.dimsuz.way.sample.android.ui.main.screen.main

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class MainViewState(
  val title: String = "Main",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
