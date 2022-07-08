package ru.dimsuz.way.sample.android.ui.{{^feature_name}}.screen.{{^folder_name}}

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class {{^screen_name}}ViewState(
  val title: String = "{{^screen_name}}",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
