package ru.dimsuz.way.sample.android.ui.login.screen.credentials

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class CredentialsViewState(
  val title: String = "Login",
  val color: Color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
)
