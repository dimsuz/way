package ru.dimsuz.way.sample.android.ui.foundation

import androidx.compose.runtime.Composable

interface Screen {
  @Composable
  fun Content()

  fun onAttach()
  fun onDetach()

  fun destroy()
}
